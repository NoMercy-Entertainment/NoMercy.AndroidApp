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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import tv.nomercy.app.shared.models.PlaylistItem

/**
 * Store for managing music playback, queue, audio focus, and crossfade logic.
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
        val intent = Intent(context, MusicPlayerService::class.java).apply {
            action = "tv.nomercy.app.action.PLAY"
        }
        context.startForegroundService(intent)
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
            Log.d("MusicPlayerStore", "Already have audio focus, skipping request")
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
                Log.d("MusicPlayerStore", "Audio focus change: $focusChange")
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        Log.d("MusicPlayerStore", "AUDIOFOCUS_LOSS - permanent loss")
                        hasAudioFocus = false
                        pause()
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        Log.d("MusicPlayerStore", "AUDIOFOCUS_LOSS_TRANSIENT - temporary loss")
                        pause()
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        Log.d("MusicPlayerStore", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK - ducking")
                        currentPlayer?.setVolume(0.3f, 0.3f)
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        Log.d("MusicPlayerStore", "AUDIOFOCUS_GAIN - gained focus")
                        hasAudioFocus = true
                        applyVolume()
                    }
                }
            }
            .build()

        val result = audioManager.requestAudioFocus(audioFocusRequest!!)
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        Log.d("MusicPlayerStore", "Audio focus request result: $result, hasAudioFocus: $hasAudioFocus")
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
            hasAudioFocus = false
            Log.d("MusicPlayerStore", "Audio focus abandoned")
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
                Log.d("MusicPlayerStore", "Prefetch triggered - remaining: $remaining ms, threshold: $prefetchLeewayMs ms")
                hasNextQueued = true
                prefetchNextTrack()
            }

            if (!isFading &&
                queue.repeatState.value != RepeatState.ONE &&
                remaining <= fadeDurationMs &&
                remaining > 0 &&
                hasNextQueued) {
                Log.d("MusicPlayerStore", "Crossfade triggered - remaining: $remaining ms, threshold: $fadeDurationMs ms")
                startCrossFade()
            }
        } catch (e: IllegalStateException) {
            Log.w("MusicPlayerStore", "Player not initialized: ${e.message}")
        }
    }

    private fun prefetchNextTrack() {
        val nextSong = queue.getNextSong() ?: return

        try {
            nextPlayer?.reset()
            val fullUrl = config.baseUrl.removeSuffix("/") + nextSong.path

            Log.d("MusicPlayerStore", "Prefetching next track: ${nextSong.name}")

            val dataSource = AuthenticatedMediaDataSource(fullUrl, config.accessToken)
            nextPlayer?.setDataSource(dataSource)
            nextPlayer?.prepareAsync()

            Log.d("MusicPlayerStore", "Next track prefetch started with authenticated data source")
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
        } catch (_: Exception) {
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
    // endregion

    // region: Playback Control
    private fun prepareSource(source: String) {
        hasNextQueued = false

        if (isFading) {
            fadeRunnable?.let { handler.removeCallbacks(it) }
            isFading = false

            try {
                nextPlayer?.stop()
                nextPlayer?.reset()
            } catch (_: Exception) {
            }
        }

        try {
            _playerState.value = PlayerState.LOADING

            try {
                if (currentPlayer?.isPlaying == true) {
                    currentPlayer?.stop()
                    _isPlaying.value = false
                }
                currentPlayer?.reset()
            } catch (e: Exception) {
                Log.w("MusicPlayerStore", "Error stopping/resetting current player: ${e.message}")
                currentPlayer?.reset()
                _isPlaying.value = false
            }

            val fullUrl = config.baseUrl.removeSuffix("/") + source

            Log.d("MusicPlayerStore", "prepareSource called")
            Log.d("MusicPlayerStore", "baseUrl: ${config.baseUrl.removeSuffix("/")}")
            Log.d("MusicPlayerStore", "source: $source")
            Log.d("MusicPlayerStore", "fullUrl: $fullUrl")
            Log.d("MusicPlayerStore", "currentPlayer: $currentPlayer")
            Log.d("MusicPlayerStore", "accessToken: ${config.accessToken?.take(20)}...")

            val dataSource = AuthenticatedMediaDataSource(fullUrl, config.accessToken)
            currentPlayer?.setDataSource(dataSource)
            currentPlayer?.prepareAsync()

            Log.d("MusicPlayerStore", "prepareAsync called successfully with authenticated data source")

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
        Log.d("MusicPlayerStore", "onPrepared called - mp: $mp, currentPlayer: $currentPlayer")

        if (mp == currentPlayer) {
            Log.d("MusicPlayerStore", "Preparing to play current player")
            _playerState.value = PlayerState.READY // Set state to READY so play() can start playback
            play()
        } else {
            Log.d("MusicPlayerStore", "Next player prepared for crossfade - not starting playback")
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        Log.d("MusicPlayerStore", "onCompletion called - mp: $mp, currentPlayer: $currentPlayer, isFading: $isFading")

        val duration = try { mp?.duration ?: 0 } catch (_: Exception) { 0 }
        val position = try { mp?.currentPosition ?: 0 } catch (_: Exception) { 0 }
        Log.d("MusicPlayerStore", "onCompletion - duration: $duration ms, position: $position ms")

        if (mp != currentPlayer) {
            Log.d("MusicPlayerStore", "onCompletion - ignoring completion from non-current player")
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
                    Log.d("MusicPlayerStore", "onCompletion - moving to next track")
                    next()
                } else {
                    Log.d("MusicPlayerStore", "onCompletion - crossfade in progress, not moving to next")
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
    }

    fun play() {
        Log.d("MusicPlayerStore", "play() called")

        val systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        Log.d("MusicPlayerStore", "System volume: $systemVolume/$maxVolume, App volume: ${_volume.value}, isMuted: ${_isMuted.value}")

        if (!requestAudioFocus()) {
            Log.d("MusicPlayerStore", "Failed to gain audio focus")
            _error.value = "Failed to gain audio focus"
            return
        }

        Log.d("MusicPlayerStore", "Audio focus gained")

        try {
            val isPlaying = currentPlayer?.isPlaying ?: false
            Log.d("MusicPlayerStore", "Current player isPlaying before start: $isPlaying")

            // Only start if not already playing and player is READY or PAUSED
            if (isPlaying) {
                Log.d("MusicPlayerStore", "Already playing, not starting again")
                return
            }
            if (_playerState.value != PlayerState.READY && _playerState.value != PlayerState.PAUSED) {
                Log.d("MusicPlayerStore", "Player is not ready or paused, cannot start playback")
                return
            }

            currentPlayer?.start()

            val isPlayingAfter = currentPlayer?.isPlaying ?: false
            Log.d("MusicPlayerStore", "Current player isPlaying after start: $isPlayingAfter")

            _isPlaying.value = true
            _playerState.value = PlayerState.PLAYING
            startTimeUpdates()

            Log.d("MusicPlayerStore", "Playback started successfully")
        } catch (e: IllegalStateException) {
            println(e.message)
            _error.value = "Failed to start playback: ${e.message}"
            _playerState.value = PlayerState.ERROR
        }
    }

    fun pause() {
        Log.d("MusicPlayerStore", "pause() called")

        try {
            currentPlayer?.pause()
            _isPlaying.value = false
            _playerState.value = PlayerState.PAUSED
            stopTimeUpdates()

            if (isFading) {
                fadeRunnable?.let { handler.removeCallbacks(it) }
                isFading = false
            }

            Log.d("MusicPlayerStore", "Paused successfully")
        } catch (e: IllegalStateException) {
            Log.e("MusicPlayerStore", "Failed to pause: ${e.message}", e)
            _error.value = "Failed to pause: ${e.message}"
        }
    }

    fun togglePlayback() {
        Log.d("MusicPlayerStore", "togglePlayback() called - current state: ${_playerState.value}, isPlaying: ${_isPlaying.value}")

        if (_isPlaying.value) {
            pause()
        } else {
            if (_playerState.value == PlayerState.PAUSED) {
                play()
            } else {
                val currentSong = queue.currentSong.value
                if (currentSong != null) {
                    Log.d("MusicPlayerStore", "Replaying current song: ${currentSong.name}")
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

    fun seekTo(positionMs: Long) {
        try {
            val duration = getDuration()
            if (duration <= 0) return

            val adjustedPosition = if (isFading) {
                val fadeAdjust = fadeDurationMs - (System.currentTimeMillis() % fadeDurationMs)
                (positionMs - fadeAdjust).coerceIn(0, duration)
            } else {
                positionMs
            }

            currentPlayer?.seekTo(adjustedPosition.toInt())
            nextPlayer?.seekTo(adjustedPosition.toInt())
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

        Log.d("MusicPlayerStore", "========================================")
        Log.d("MusicPlayerStore", "playTrack called")
        Log.d("MusicPlayerStore", "track.path: ${track.path}")
        Log.d("MusicPlayerStore", "track.name: ${track.name}")
        Log.d("MusicPlayerStore", "tracks size: ${tracks?.size}")
        Log.d("MusicPlayerStore", "playlistId: $playlistId")
        Log.d("MusicPlayerStore", "current playlistId: ${_currentPlaylistId.value}")
        Log.d("MusicPlayerStore", "========================================")

        val isDifferentPlaylist = playlistId != null && _currentPlaylistId.value != playlistId
        if (isDifferentPlaylist) {

            Log.d("MusicPlayerStore", "Switching to new playlist - stopping current playback and clearing queue")
            try {
                currentPlayer?.stop()
                currentPlayer?.reset()
                nextPlayer?.stop()
                nextPlayer?.reset()

                _isPlaying.value = false
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
    }

    fun isCurrentPlaylist(playlistId: String?): Boolean {
        return _currentPlaylistId.value == playlistId
    }

    fun next() {
        Log.d("MusicPlayerStore", "next() called - isFading: $isFading")

        if (isFading) {
            Log.d("MusicPlayerStore", "Completing crossfade before next")
            completeCrossFade()
        } else {
            val nextSong = queue.moveToNext()
            if (nextSong != null) {
                Log.d("MusicPlayerStore", "Moving to next song: ${nextSong.name}")
                prepareSource(nextSong.path)
            } else {
                Log.w("MusicPlayerStore", "No next song found")
            }
        }
    }

    fun previous() {
        Log.d("MusicPlayerStore", "previous() called")

        val previousSong = queue.getPreviousSong() ?: return

        Log.d("MusicPlayerStore", "Playing previous song: ${previousSong.name}")
        prepareSource(previousSong.path)
    }

    fun setShuffle(enabled: Boolean) {
        queue.setShuffle(enabled)
    }

    fun setRepeat(repeatMode: RepeatState) {
        queue.setRepeat(repeatMode)
    }

    fun seekForward() {
        val newPosition = (getPosition() + 15000).coerceAtMost(getDuration())
        seekTo(newPosition)
    }

    fun seekBackward() {
        val newPosition = (getPosition() - 15000).coerceAtLeast(0)
        seekTo(newPosition)
    }

    object MusicPlayerStoreHolder {
        @SuppressLint("StaticFieldLeak")
        private var instance: MusicPlayerStore? = null
        fun getInstance(context: Context, config: MusicPlayerConfig): MusicPlayerStore {
            if (instance == null) {
                instance = MusicPlayerStore(context.applicationContext, config)
            }
            return instance!!
        }
    }
}
