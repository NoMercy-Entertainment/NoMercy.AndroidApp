// kotlin
package tv.nomercy.app.shared.stores.musicPlayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import androidx.media.session.MediaButtonReceiver
import coil3.ImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log
import coil3.request.allowHardware
import kotlinx.coroutines.cancel
import tv.nomercy.app.R
import tv.nomercy.app.shared.stores.GlobalStores.getServerConfigStore
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.api.KeycloakConfig.getSuffix
import tv.nomercy.app.shared.models.PlaylistItem

class MusicPlayerService : Service() {

    private lateinit var mediaSession: MediaSessionCompat
    private val _channelId = "music_player_channel"
    private val _notificationId = 1

    private lateinit var playerStore: MusicPlayerStore

    private var observeJob: Job? = null
    private var periodicJob: Job? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val imageLoader by lazy { ImageLoader(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        mediaSession = MediaSessionCompat(this, "MusicPlayerService").apply {
            isActive = true
            val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON).setClass(this@MusicPlayerService, MediaButtonReceiver::class.java)
            val mediaButtonPending = PendingIntent.getBroadcast(
                this@MusicPlayerService,
                0,
                mediaButtonIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            setMediaButtonReceiver(mediaButtonPending)

            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = handlePlay()
                override fun onPause() = handlePause()
                override fun onSkipToNext() = handleNext()
                override fun onSkipToPrevious() = handlePrev()
                override fun onSeekTo(pos: Long) {
                    playerStore.seekTo(pos)
                    updateMediaSessionState(playerStore.isPlaying.value, playerStore.currentSong.value)
                    updateNotificationAsync()
                }
                override fun onFastForward() {
                    seekRelative(SEEK_STEP_MS)
                }
                override fun onRewind() {
                    seekRelative(-SEEK_STEP_MS)
                }
                override fun onCustomAction(action: String?, extras: android.os.Bundle?) {
                    when (action) {
                        ACTION_FAST_FORWARD -> seekRelative(SEEK_STEP_MS)
                        ACTION_REWIND -> seekRelative(-SEEK_STEP_MS)
                        else -> super.onCustomAction(action, extras)
                    }
                }
            })
        }

        playerStore = GlobalStores.getMusicPlayerStore(applicationContext)

        startObservingPlayerState()

        periodicJob = serviceScope.launch {
            while (true) {
                if (playerStore.isPlaying.value) {
                    updateMediaSessionState(true, playerStore.currentSong.value)
                    updateNotificationAsync()
                }
                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        observeJob?.cancel()
        periodicJob?.cancel()
        serviceScope.coroutineContext.cancel()
        mediaSession.release()
        super.onDestroy()
    }

    private fun startObservingPlayerState() {
        observeJob?.cancel()
        observeJob = serviceScope.launch {
            playerStore.isPlaying.collectLatest { isPlaying ->
                updateMediaSessionState(isPlaying, playerStore.currentSong.value)
                updateNotificationAsync()
            }
        }

        serviceScope.launch {
            playerStore.currentSong.collectLatest { item ->
                updateMediaSessionState(playerStore.isPlaying.value, item)
                updateNotificationAsync()
            }
        }
    }

    private fun seekRelative(deltaMs: Long) {
        val ts = playerStore.timeState.value
        val duration = ts.duration
        val current = ts.position
        val newPosition = (current + deltaMs).coerceIn(0L, if (duration > 0L) duration else Long.MAX_VALUE)
        playerStore.seekTo(newPosition)
        updateMediaSessionState(playerStore.isPlaying.value, playerStore.currentSong.value)
        updateNotificationAsync()
    }

    private fun updateMediaSessionState(isPlaying: Boolean, currentSong: PlaylistItem?) {
        val timeState = playerStore.timeState.value
        val position = timeState.position
        val duration = timeState.duration

        val actions = (PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_SEEK_TO
                or PlaybackStateCompat.ACTION_FAST_FORWARD
                or PlaybackStateCompat.ACTION_REWIND)

        val state = PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                position,
                1.0f
            ).build()

        mediaSession.setPlaybackState(state)

        serviceScope.launch(Dispatchers.IO) {
            val builder = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong?.name ?: getString(R.string.app_name))
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong?.artistTrack?.firstOrNull()?.name ?: "")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)

            val cover = currentSong?.cover
            val resolvedBitmap = try {
                if (!cover.isNullOrBlank()) getAlbumArtBitmap(cover, this@MusicPlayerService)
                else null
            } catch (t: Throwable) {
                Log.w(TAG, "Error loading album art", t)
                null
            }

            if (resolvedBitmap != null) {
                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resolvedBitmap)
                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, resolvedBitmap)
            }

            mediaSession.setMetadata(builder.build())
        }
    }

    private suspend fun createNotificationWithArtSync(): Notification {
        val isPlaying = playerStore.isPlaying.value
        val currentSong = playerStore.currentSong.value
        val title = currentSong?.name ?: getString(R.string.app_name)
        val artist = currentSong?.artistTrack?.firstOrNull()?.name ?: ""
        val timeState = playerStore.timeState.value
        val duration = timeState.duration.toInt()
        val position = timeState.position.toInt()
        val showProgress = duration > 0 && position in 0..duration

        val playPauseIcon = if (isPlaying) R.drawable.nmpausesolid else R.drawable.nmplaysolid
        val playPauseAction = if (isPlaying) ACTION_PAUSE else ACTION_PLAY

        val playPauseIntent = pendingServiceIntent(playPauseAction, requestCode = 0)
        val nextIntent = pendingServiceIntent(ACTION_NEXT, requestCode = 2)
        val prevIntent = pendingServiceIntent(ACTION_PREV, requestCode = 3)
        val ffIntent = pendingServiceIntent(ACTION_FAST_FORWARD, requestCode = 4)
        val rwIntent = pendingServiceIntent(ACTION_REWIND, requestCode = 5)

        val builder = NotificationCompat.Builder(this, _channelId)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.ic_mediasession_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.nmrewindhalftone, "Rewind", rwIntent)
            .addAction(R.drawable.nmprevioushalftone, "Previous", prevIntent)
            .addAction(playPauseIcon, if (isPlaying) "Pause" else "Play", playPauseIntent)
            .addAction(R.drawable.nmforwardhalftone, "Next", nextIntent)
            .addAction(R.drawable.nmforwardhalftone, "Fast Forward", ffIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(1, 2, 3)
            )

        val metadata = mediaSession.controller.metadata
        val albumArt = metadata?.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
        if (albumArt != null) {
            builder.setLargeIcon(albumArt)
        } else {
            val cover = currentSong?.cover
            val artBitmap = try {
                if (!cover.isNullOrBlank()) getAlbumArtBitmap(cover, this) else null
            } catch (t: Throwable) {
                Log.w(TAG, "Quick album art load failed", t)
                null
            }
            if (artBitmap != null) builder.setLargeIcon(artBitmap)
        }

        if (showProgress) builder.setProgress(duration, position, false) else builder.setProgress(0, 0, false)
        return builder.build()
    }

    private fun updateNotificationAsync() {
        serviceScope.launch {
            val notification = createNotificationWithArtSync()
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (!isForegroundServiceActive()) {
                startForeground(_notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                manager.notify(_notificationId, notification)
            }
        }
    }

    private suspend fun getAlbumArtBitmap(cover: String?, context: Context): Bitmap? {
        val serverConfigStore = getServerConfigStore(context)
        val currentServer = serverConfigStore.currentServer.value
        val serverBaseUrl = currentServer?.serverBaseUrl?.trimEnd('/')
        val url = resolveImageUrlWithSize(cover, serverBaseUrl, 512) ?: return null

        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()

        val result = imageLoader.execute(request)
        return if (result is SuccessResult) {
            val drawable = result.image.asDrawable(resources = context.resources)
            if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                try {
                    val w = (drawable.intrinsicWidth.takeIf { it > 0 } ?: 256)
                    val h = (drawable.intrinsicHeight.takeIf { it > 0 } ?: 256)
                    val bitmap = createBitmap(w, h)
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap
                } catch (e: Exception) {
                    Log.w(TAG, "Drawable -> Bitmap conversion failed", e)
                    null
                }
            }
        } else null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> handlePlay()
            ACTION_PAUSE -> handlePause()
            ACTION_NEXT -> handleNext()
            ACTION_PREV -> handlePrev()
            ACTION_SEEK_FORWARD, ACTION_FAST_FORWARD -> seekRelative(SEEK_STEP_MS)
            ACTION_SEEK_BACKWARD, ACTION_REWIND -> seekRelative(-SEEK_STEP_MS)
            ACTION_PLAY_TRACK -> {
                Log.w(TAG, "ACTION_PLAY_TRACK not implemented: playlist/tracks logic is missing")
            }
            else -> {}
        }

        updateNotificationAsync()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun handlePlay() = playerStore.play()
    private fun handlePause() = playerStore.pause()
    private fun handleNext() = playerStore.next()
    private fun handlePrev() = playerStore.previous()

    private fun pendingServiceIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, MusicPlayerService::class.java).setAction(action)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getService(this, requestCode, intent, flags)
    }

    private fun isForegroundServiceActive(): Boolean {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        return try {
            manager.activeNotifications.any { it.id == _notificationId }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to check active notifications", t)
            false
        }
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(_channelId)
        if (existing == null) {
            val channel = NotificationChannel(
                _channelId,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun resolveImageUrlWithSize(cover: String?, serverBaseUrl: String?, size: Int = 256): String? {
        if (cover.isNullOrBlank()) return null
        val suffix = getSuffix()

        val baseUrl = when {
            cover.startsWith("https://") && "fanart.tv" in cover -> {
                val proxy = "https://api$suffix.nomercy.tv/cors?url="
                proxy + cover
            }
            cover.startsWith("https://") -> cover
            cover.startsWith("/") && !serverBaseUrl.isNullOrBlank() -> "$serverBaseUrl$cover"
            else -> return null
        }

        val separator = if (baseUrl.contains("?")) "&" else "?"
        return baseUrl + separator + "width=$size&type=png&aspect_ratio=1"
    }

    companion object {
        private const val TAG = "MusicPlayerService"
        private const val SEEK_STEP_MS = 10_000L

        private const val ACTION_PLAY = "tv.nomercy.app.action.PLAY"
        private const val ACTION_PAUSE = "tv.nomercy.app.action.PAUSE"
        private const val ACTION_NEXT = "tv.nomercy.app.action.NEXT"
        private const val ACTION_PREV = "tv.nomercy.app.action.PREV"
        private const val ACTION_PLAY_TRACK = "tv.nomercy.app.action.PLAY_TRACK"
        private const val ACTION_SEEK_FORWARD = "tv.nomercy.app.action.SEEK_FORWARD"
        private const val ACTION_SEEK_BACKWARD = "tv.nomercy.app.action.SEEK_BACKWARD"
        private const val ACTION_FAST_FORWARD = "tv.nomercy.app.action.FAST_FORWARD"
        private const val ACTION_REWIND = "tv.nomercy.app.action.REWIND"
    }
}
