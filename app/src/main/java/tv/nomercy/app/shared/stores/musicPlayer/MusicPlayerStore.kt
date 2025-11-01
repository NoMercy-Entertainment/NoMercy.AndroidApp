package tv.nomercy.app.shared.stores.musicPlayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import tv.nomercy.app.shared.models.PlaylistItem
import kotlin.math.max
import kotlin.math.min

/**
 * Store for managing music playback, queue, audio focus, and crossfade logic.
 *
 * Changes:
 * - previous() now: if current position > 3s -> seek to 0 and keep playing; otherwise go to previous track.
 * - seekTo(...) updates time state once seek completes (onSeekComplete).
 * - onSeekComplete implemented to refresh TimeState and player flags.
 * - Defensive, small cleanup to make seeking behavior more reliable for remote fast-forward/rewind.
 */
class MusicPlayerStore(
    private val context: Context,
    private val config: MusicPlayerConfig,
) : MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnSeekCompleteListener {

    // region: State & Properties
    private val queue = MusicPlayerQueue()
    private var mediaPlayer1: MediaPlayer? = null
    private var mediaPlayer2: MediaPlayer? = null
    private var currentPlayer: MediaPlayer? = null
    private var nextPlayer: MediaPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    private val handler = Handler(Looper.getMainLooper())
    private var timeUpdateRunnable: Runnable? = null
    private var fadeRunnable: Runnable? = null
    private var isFading = false
    private var hasNextQueued = false
    private var fadeInVolume = 0
    private var fadeOutVolume = 100
    private val crossFadeSteps = config.crossFadeSteps
    private val prefetchLeewayMs = config.prefetchLeeway * 1000L
    private val fadeDurationMs = config.fadeDuration * 1000L

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(100)
    val volume: StateFlow<Int> = _volume.asStateFlow()

    private val _volumeState = MutableStateFlow(VolumeState.UNMUTED)
    val volumeState: StateFlow<VolumeState> = _volumeState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _timeState = MutableStateFlow(TimeState())
    val timeState: StateFlow<TimeState> = _timeState.asStateFlow()

    private val _bufferedPercentage = MutableStateFlow(0)
    val bufferedPercentage: StateFlow<Int> = _bufferedPercentage.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentPlaylistId = MutableStateFlow<String?>(null)
    val currentPlaylistId: StateFlow<String?> = _currentPlaylistId.asStateFlow()

    private val _isFullPlayerOpen = MutableStateFlow(false)
    val isFullPlayerOpen: StateFlow<Boolean> = _isFullPlayerOpen.asStateFlow()

    private val _showingLyrics = MutableStateFlow(false)
    val showingLyrics: StateFlow<Boolean> = _showingLyrics.asStateFlow()

    val currentSong = queue.currentSong
    val queueList = queue.queue
    val backlog = queue.backlog
    val isShuffling = queue.isShuffling
    val repeatState = queue.repeatState
    // endregion

    init {
        initializeMediaPlayers()
        loadVolumeFromPreferences()
    }

    // region: Initialization & Configuration
    private fun ensureServiceRunning() {
        try {
            val intent = Intent(context, MusicPlayerService::class.java).apply {
                action = MusicPlayerService.ACTION_START_SERVICE
            }
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            Log.w("MusicPlayerStore", "Failed to start MusicPlayerService: ${e.message}")
        }
    }

    private fun initializeMediaPlayers() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        mediaPlayer1 = MediaPlayer().apply {
            setAudioAttributes(audioAttributes)
            setOnPreparedListener(this@MusicPlayerStore)
            setOnCompletionListener(this@MusicPlayerStore)
            setOnErrorListener(this@MusicPlayerStore)
            setOnBufferingUpdateListener(this@MusicPlayerStore)
            setOnSeekCompleteListener(this@MusicPlayerStore)
            setWakeMode(context, android.os.PowerManager.PARTIAL_WAKE_LOCK)
        }

        mediaPlayer2 = MediaPlayer().apply {
            setAudioAttributes(audioAttributes)
            setOnPreparedListener(this@MusicPlayerStore)
            setOnCompletionListener(this@MusicPlayerStore)
            setOnErrorListener(this@MusicPlayerStore)
            setOnBufferingUpdateListener(this@MusicPlayerStore)
            setOnSeekCompleteListener(this@MusicPlayerStore)
            setWakeMode(context, android.os.PowerManager.PARTIAL_WAKE_LOCK)
        }

        currentPlayer = mediaPlayer1
        nextPlayer = mediaPlayer2

        applyVolume()
    }

    private fun loadVolumeFromPreferences() {
        val prefs = context.getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE)
        val savedVolume = prefs.getInt("volume", 100)
        val savedMuted = prefs.getBoolean("muted", false)

        _volume.value = savedVolume
        _isMuted.value = savedMuted
        _volumeState.value = if (savedMuted) VolumeState.MUTED else VolumeState.UNMUTED

        applyVolume()
    }

    private fun saveVolumeToPreferences() {
        val prefs = context.getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("volume", _volume.value)
            putBoolean("muted", _isMuted.value)
            apply()
        }
    }

    private fun applyVolume() {
        val volumeLevel = if (_isMuted.value) 0f else _volume.value / 100f

        if (!isFading) {
            currentPlayer?.setVolume(volumeLevel, volumeLevel)
            nextPlayer?.setVolume(volumeLevel, volumeLevel)
        } else {
            val outLevel = if (_isMuted.value) 0f else fadeOutVolume / 100f
            val inLevel = if (_isMuted.value) 0f else fadeInVolume / 100f
            currentPlayer?.setVolume(outLevel, outLevel)
            nextPlayer?.setVolume(inLevel, inLevel)
        }
    }
    // endregion

    // region: Audio Focus
    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) {
            return true
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        hasAudioFocus = false
                        pause()
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        pause()
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        currentPlayer?.setVolume(0.3f, 0.3f)
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        hasAudioFocus = true
                        applyVolume()
                    }
                }
            }
            .build()

        val result = audioManager.requestAudioFocus(audioFocusRequest!!)
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
            hasAudioFocus = false
        }
    }
    // endregion

    // region: Time Updates
    private fun startTimeUpdates() {
        stopTimeUpdates()
        timeUpdateRunnable = object : Runnable {
            override fun run() {
                updateTimeState()
                handler.postDelayed(this, 200)
            }
        }
        handler.post(timeUpdateRunnable!!)
    }

    private fun stopTimeUpdates() {
        timeUpdateRunnable?.let {
            handler.removeCallbacks(it)
        }
        timeUpdateRunnable = null
    }

    private fun updateTimeState() {
        val player = currentPlayer ?: return
        if (!player.isPlaying && _playerState.value != PlayerState.PAUSED) return

        try {
            val duration = player.duration.toLong()
            val position = player.currentPosition.toLong()

            if (duration <= 0) {
                Log.w("MusicPlayerStore", "Invalid duration: $duration ms, position: $position ms")
                return
            }

            val percentage = position.toFloat() / duration.toFloat()
            val remaining = duration - position

            _timeState.value = TimeState(
                buffered = (_bufferedPercentage.value * duration / 100),
                duration = duration,
                percentage = percentage,
                position = position,
                remaining = remaining
            )

            if (player.isPlaying && _playerState.value != PlayerState.PLAYING) {
                _playerState.value = PlayerState.PLAYING
            }

            if (!hasNextQueued &&
                queue.repeatState.value != RepeatState.ONE &&
                remaining <= prefetchLeewayMs &&
                remaining > 0) {
                hasNextQueued = true
                prefetchNextTrack()
            }

            if (!isFading &&
                queue.repeatState.value != RepeatState.ONE &&
                remaining <= fadeDurationMs &&
                remaining > 0 &&
                hasNextQueued) {
                startCrossFade()
            }
        } catch (e: IllegalStateException) {
            Log.w("MusicPlayerStore", "Player not initialized: ${e.message}")
        }
    }
    // endregion

    private fun prefetchNextTrack() {
        val nextSong = queue.getNextSong() ?: return

        try {
            nextPlayer?.reset()
            val songPath = nextSong.path
            val fullUrl = if (songPath.startsWith("http://") || songPath.startsWith("https://")) {
                songPath
            } else {
                config.baseUrl.removeSuffix("/") + songPath
            }


            val dataSource = AuthenticatedMediaDataSource(fullUrl, config.accessToken)
            nextPlayer?.setDataSource(dataSource)
            nextPlayer?.prepareAsync()

        } catch (e: Exception) {
            Log.e("MusicPlayerStore", "Failed to prefetch next track: ${e.message}", e)
            hasNextQueued = false
        }
    }

    private fun startCrossFade() {
        if (isFading || nextPlayer == null) return

        isFading = true
        fadeOutVolume = _volume.value
        fadeInVolume = 0

        try {
            nextPlayer?.setVolume(0f, 0f)
            nextPlayer?.start()
            performCrossFade()
        } catch (e: Exception) {
            isFading = false
            _error.value = "Crossfade failed: ${e.message}"
        }
    }

    private fun performCrossFade() {
        fadeRunnable = object : Runnable {
            override fun run() {
                if (!isFading) return

                val volumeLevel = if (_isMuted.value) 0 else _volume.value

                if (fadeOutVolume > 0) {
                    fadeOutVolume -= crossFadeSteps
                    if (fadeOutVolume < 0) fadeOutVolume = 0

                    val outLevel = if (_isMuted.value) 0f else fadeOutVolume / 100f
                    currentPlayer?.setVolume(outLevel, outLevel)
                }

                if (fadeInVolume < volumeLevel) {
                    fadeInVolume += crossFadeSteps
                    if (fadeInVolume > volumeLevel) fadeInVolume = volumeLevel

                    val inLevel = if (_isMuted.value) 0f else fadeInVolume / 100f
                    nextPlayer?.setVolume(inLevel, inLevel)
                }

                if (fadeOutVolume > 0 || fadeInVolume < volumeLevel) {
                    handler.postDelayed(this, 200)
                } else {
                    completeCrossFade()
                }
            }
        }
        handler.post(fadeRunnable!!)
    }

    private fun completeCrossFade() {
        try {
            currentPlayer?.pause()
            currentPlayer?.reset()
        } catch (e: Exception) {
            Log.w("MusicPlayerStore", "Error resetting current player after crossfade: ${e.message}")
        }

        val temp = currentPlayer
        currentPlayer = nextPlayer
        nextPlayer = temp

        queue.moveToNext()

        isFading = false
        hasNextQueued = false
        fadeInVolume = _volume.value
        fadeOutVolume = 0

        fadeRunnable?.let { handler.removeCallbacks(it) }
        fadeRunnable = null
    }

    // region: Playback Control
    private fun prepareSource(source: String) {
        hasNextQueued = false

        if (isFading) {
            fadeRunnable?.let { handler.removeCallbacks(it) }
            isFading = false

            try {
                nextPlayer?.stop()
                nextPlayer?.reset()
            } catch (e: Exception) {
                Log.w("MusicPlayerStore", "Error stopping/resetting next player during prepareSource: ${e.message}")
            }
        }

        try {
            _playerState.value = PlayerState.LOADING

            try {
                if (currentPlayer?.isPlaying == true) {
                    currentPlayer?.stop()
                    _timeState.value = TimeState()
                    _isPlaying.value = false
                }
                currentPlayer?.reset()
            } catch (e: Exception) {
                Log.w("MusicPlayerStore", "Error stopping/resetting current player: ${e.message}")
                currentPlayer?.reset()
                _isPlaying.value = false
            }

            val sourcePath = source
            val fullUrl = if (sourcePath.startsWith("http://") || sourcePath.startsWith("https://")) {
                sourcePath
            } else {
                config.baseUrl.removeSuffix("/") + sourcePath
            }

            val dataSource = AuthenticatedMediaDataSource(fullUrl, config.accessToken)
            currentPlayer?.setDataSource(dataSource)
            currentPlayer?.prepareAsync()

            applyVolume()

        } catch (e: IOException) {
            Log.e("MusicPlayerStore", "IOException: ${e.message}", e)
            _error.value = "Failed to load audio: ${e.message}"
            _playerState.value = PlayerState.ERROR
        } catch (e: IllegalStateException) {
            Log.e("MusicPlayerStore", "IllegalStateException: ${e.message}", e)
            _error.value = "Player error: ${e.message}"
            _playerState.value = PlayerState.ERROR
        } catch (e: Exception) {
            Log.e("MusicPlayerStore", "Exception: ${e.message}", e)
            _error.value = "Unexpected error: ${e.message}"
            _playerState.value = PlayerState.ERROR
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        if (mp == currentPlayer) {
            _playerState.value = PlayerState.READY // Set state to READY so play() can start playback
            play()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (mp != currentPlayer) {
            return
        }

        _playerState.value = PlayerState.ENDED
        _isPlaying.value = false

        when (repeatState.value) {
            RepeatState.ONE -> {
                seekTo(0)
                play()
            }
            RepeatState.ALL, RepeatState.OFF -> {
                if (!isFading) {
                    next()
                } else {
                }
            }
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.e("MusicPlayerStore", "MediaPlayer error - what=$what, extra=$extra, player=$mp")

        if (mp == currentPlayer) {
            _error.value = "MediaPlayer error: what=$what, extra=$extra"
            _playerState.value = PlayerState.ERROR
            _isPlaying.value = false
            Log.e("MusicPlayerStore", "Current player error - stopping playback")
        } else {
            Log.w("MusicPlayerStore", "Next player error - resetting prefetch")
            hasNextQueued = false
        }

        return true
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        _bufferedPercentage.value = percent
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        // Refresh time state and player flags after async seek completes.
        try {
            val player = if (mp == currentPlayer) currentPlayer else mp
            val duration = player?.duration?.toLong() ?: 0L
            val position = player?.currentPosition?.toLong() ?: 0L
            val percentage = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
            val remaining = if (duration > 0) duration - position else 0L

            _timeState.value = TimeState(
                buffered = (_bufferedPercentage.value * duration / 100),
                duration = duration,
                percentage = percentage,
                position = position,
                remaining = remaining
            )
        } catch (e: Exception) {
            Log.w("MusicPlayerStore", "onSeekComplete: failed to refresh time state: ${e.message}")
        }
    }

    fun play() {
        ensureServiceRunning()

        if (!requestAudioFocus()) {
            _error.value = "Failed to gain audio focus"
            return
        }


        try {
            val isPlaying = currentPlayer?.isPlaying ?: false

            // Only start if not already playing and player is READY or PAUSED
            if (isPlaying) {
                return
            }
            if (_playerState.value != PlayerState.READY && _playerState.value != PlayerState.PAUSED) {
                return
            }

            currentPlayer?.start()

            val isPlayingAfter = currentPlayer?.isPlaying ?: false

            _isPlaying.value = true
            _playerState.value = PlayerState.PLAYING
            startTimeUpdates()

        } catch (e: IllegalStateException) {
            println(e.message)
            _error.value = "Failed to start playback: ${e.message}"
            _playerState.value = PlayerState.ERROR
        }
    }

    fun pause() {

        try {
            currentPlayer?.pause()
            _isPlaying.value = false
            _playerState.value = PlayerState.PAUSED
            stopTimeUpdates()

            if (isFading) {
                fadeRunnable?.let { handler.removeCallbacks(it) }
                isFading = false
            }

        } catch (e: IllegalStateException) {
            Log.e("MusicPlayerStore", "Failed to pause: ${e.message}", e)
            _error.value = "Failed to pause: ${e.message}"
        }
    }

    fun togglePlayback() {

        if (_isPlaying.value) {
            pause()
        } else {
            if (_playerState.value == PlayerState.PAUSED) {
                play()
            } else {
                val currentSong = queue.currentSong.value
                if (currentSong != null) {
                    ensureServiceRunning()
                    prepareSource(currentSong.path)
                } else {
                    Log.w("MusicPlayerStore", "No current song to play")
                }
            }
        }
    }

    fun stop() {
        try {
            currentPlayer?.stop()
            currentPlayer?.reset()
            nextPlayer?.stop()
            nextPlayer?.reset()

            _isPlaying.value = false
            _playerState.value = PlayerState.STOPPED
            _timeState.value = TimeState()
            stopTimeUpdates()
            abandonAudioFocus()

            if (isFading) {
                fadeRunnable?.let { handler.removeCallbacks(it) }
                isFading = false
            }
            hasNextQueued = false

            queue.setCurrentSong(null)
            queue.clearQueue()
            queue.clearBacklog()
        } catch (e: IllegalStateException) {
            _error.value = "Failed to stop: ${e.message}"
        }
    }

    /**
     * Seek to an absolute position in milliseconds.
     * Attempts to seek both players (current and next) so crossfade/prefetch remain in sync.
     * Updates time state via onSeekComplete when available.
     */
    fun seekTo(positionMs: Long) {
        try {
            val duration = getDuration()
            if (duration <= 0) return

            val adjustedPosition = if (isFading) {
                // During fade we try to avoid seeking past the fade boundary, but keep it simple:
                positionMs.coerceIn(0L, duration)
            } else {
                positionMs.coerceIn(0L, duration)
            }

            currentPlayer?.seekTo(adjustedPosition.toInt())
            // nextPlayer may be null or not prepared, guard it
            try {
                nextPlayer?.seekTo(adjustedPosition.toInt())
            } catch (_: Exception) {
            }

            // Optimistically update time state immediately for snappy UI; onSeekComplete will refresh authoritative values
            val dur = duration
            val pos = adjustedPosition
            val perc = pos.toFloat() / dur.toFloat()
            val remaining = dur - pos
            _timeState.value = TimeState(
                buffered = (_bufferedPercentage.value * dur / 100),
                duration = dur,
                percentage = perc,
                position = pos,
                remaining = remaining
            )
        } catch (e: Exception) {
            Log.e("MusicPlayerStore", "Seek error: ${e.message}", e)
        }
    }

    fun getDuration(): Long {
        return currentPlayer?.duration?.toLong() ?: 0
    }

    fun getPosition(): Long {
        return currentPlayer?.currentPosition?.toLong() ?: 0
    }

    fun playTrack(track: PlaylistItem, tracks: List<PlaylistItem>? = null, playlistId: String? = null) {
        // Ensure the service is running so the widget/notification appears
        ensureServiceRunning()


        val isDifferentPlaylist = playlistId != null && _currentPlaylistId.value != playlistId
        if (isDifferentPlaylist) {

            try {
                currentPlayer?.stop()
                currentPlayer?.reset()
                nextPlayer?.stop()
                nextPlayer?.reset()

                _isPlaying.value = false
                _timeState.value = TimeState()
                stopTimeUpdates()

                if (isFading) {
                    fadeRunnable?.let { handler.removeCallbacks(it) }
                    isFading = false
                }
                hasNextQueued = false

                queue.clearQueue()
                queue.clearBacklog()
            } catch (e: Exception) {
                Log.w("MusicPlayerStore", "Error stopping current playback: ${e.message}")
            }
        }

        _currentPlaylistId.value = playlistId

        queue.playTrack(track, tracks)
        prepareSource(track.path)

        if(tracks?.isNotEmpty() == true){
            _isFullPlayerOpen.value = true
        }
    }

    fun isCurrentPlaylist(playlistId: String?): Boolean {
        return _currentPlaylistId.value == playlistId
    }

    fun next() {
        if (isFading) {
            completeCrossFade()
        } else {
            _timeState.value = _timeState.value.copy(position = 0L)
            val nextSong = queue.moveToNext()
            if (nextSong != null) {
                prepareSource(nextSong.path)
            } else {
                Log.w("MusicPlayerStore", "No next song found")
            }
        }
    }

    /**
     * Behavior requested by user:
     * - If current position is later than 3 seconds -> set time back to 0 and keep playing the same track.
     * - Otherwise go to previous track.
     */
    fun previous() {
        val currentPosition = getPosition()
        val threeSecondsMs = 3000L

        // If we're further than 3s into the track, restart the current track
        if (currentPosition > threeSecondsMs) {
            Log.d("MusicPlayerStore", "Position > 3s ($currentPosition ms): seeking to 0")
            seekTo(0L)
            if (_isPlaying.value) {
                _playerState.value = PlayerState.PLAYING
            } else {
                _playerState.value = PlayerState.PAUSED
            }
            return
        }

        // Otherwise, attempt to move the queue to the previous track (this mutates queue state)
        val previousSong = queue.moveToPrevious()
        if (previousSong == null) {
            Log.w("MusicPlayerStore", "No previous song available, restarting current track")
            seekTo(0L)
            return
        }

        Log.d("MusicPlayerStore", "Switching to previous song: ${previousSong.name}")
        // Reset time state and prepare the new source
        _timeState.value = _timeState.value.copy(position = 0L, percentage = 0f, remaining = 0L)
        prepareSource(previousSong.path)
    }
    // endregion

    fun setShuffle(enabled: Boolean) {
        queue.setShuffle(enabled)
    }

    fun setRepeat(repeatMode: RepeatState) {
        queue.setRepeat(repeatMode)
    }

    fun openFullPlayer() {
        _isFullPlayerOpen.value = true
    }

    fun closeFullPlayer() {
        _isFullPlayerOpen.value = false
    }

    fun toggleLyrics() {
        _showingLyrics.value = !_showingLyrics.value
    }

}
