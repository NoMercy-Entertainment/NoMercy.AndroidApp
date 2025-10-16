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
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.app.NotificationCompat
import coil3.ImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log
import tv.nomercy.app.R
import tv.nomercy.app.shared.stores.GlobalStores.getServerConfigStore
import androidx.core.graphics.createBitmap
import androidx.media.session.MediaButtonReceiver
import kotlinx.coroutines.delay
import tv.nomercy.app.shared.api.KeycloakConfig.getSuffix
import tv.nomercy.app.shared.models.PlaylistItem
import tv.nomercy.app.shared.stores.GlobalStores

/**
 * Foreground service for music playback, notification, and media session management.
 */
class MusicPlayerService : Service() {
    // region: State & Properties
    private lateinit var mediaSession: MediaSessionCompat
    private val CHANNEL_ID = "music_player_channel"
    private val NOTIFICATION_ID = 1
    private lateinit var playerStore: MusicPlayerStore
    private var notificationJob: Job? = null
    private var progressJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    // endregion

    // region: Lifecycle
    override fun onCreate() {
        super.onCreate()
        Log.d("MusicPlayerService", "onCreate called")
        createNotificationChannel()
        mediaSession = MediaSessionCompat(this, "MusicPlayerService")
        mediaSession.isActive = true
        // Register media button receiver for system widget integration
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)
        val mediaButtonPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE)
        mediaSession.setMediaButtonReceiver(mediaButtonPendingIntent)
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() { handlePlay() }
            override fun onPause() { handlePause() }
            override fun onSkipToNext() { handleNext() }
            override fun onSkipToPrevious() { handlePrev() }
            override fun onSeekTo(pos: Long) {
                Log.d("MusicPlayerService", "onSeekTo called: $pos")
                playerStore.seekTo(pos)
                // Immediately update state after seek
                updateMediaSessionState(playerStore.isPlaying.value, playerStore.currentSong.value)
            }
        })
        val context = applicationContext
        playerStore = GlobalStores.getMusicPlayerStore(context)
        observePlayerState()
        // Start a timer to update MediaSession state every second while playing
        serviceScope.launch {
            while (true) {
                if (playerStore.isPlaying.value) {
                    updateMediaSessionState(true, playerStore.currentSong.value)
                }
                delay(1000)
            }
        }
    }

    private fun updateMediaSessionState(isPlaying: Boolean, currentSong: PlaylistItem?) {
        // Get progress info from timeState
        val timeState = playerStore.timeState.value
        val position = timeState.position
        val duration = timeState.duration
        Log.d("MusicPlayerService", "updateMediaSessionState: position=$position, duration=$duration")
        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                position,
                1.0f
            )
        mediaSession.setPlaybackState(stateBuilder.build())

        // Set metadata with album art and duration (async)
        serviceScope.launch {
            val builder = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong?.name ?: "Music Player")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong?.artistTrack?.firstOrNull()?.name ?: "")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
            val cover = currentSong?.cover
            var artBitmap: Bitmap? = null
            var resolvedUrl: String?
            if (cover != null) {
                val serverConfigStore = getServerConfigStore(this@MusicPlayerService)
                val currentServer = serverConfigStore.currentServer.value
                val serverBaseUrl = currentServer?.serverBaseUrl?.trimEnd('/')
                resolvedUrl = resolveImageUrlWithSize(cover, serverBaseUrl, 256)
                Log.d("MusicPlayerService", "Resolved album art URL: $resolvedUrl")
                if (!resolvedUrl.isNullOrBlank()) {
                    artBitmap = getAlbumArtBitmap(cover, this@MusicPlayerService)
                    if (artBitmap == null) {
                        Log.w("MusicPlayerService", "Failed to load album art bitmap from $resolvedUrl")
                    }
                }
            }
            if (artBitmap != null) {
                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artBitmap)
                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, artBitmap)
            } else {
                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null)
                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, null)
            }
            mediaSession.setMetadata(builder.build())
        }
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return
        progressJob = serviceScope.launch {
            while (playerStore.isPlaying.value) {
                updateNotification()
                delay(1000)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun observePlayerState() {
        notificationJob?.cancel()
        notificationJob = serviceScope.launch {
            playerStore.isPlaying.collectLatest { isPlaying ->
                updateNotification()
                updateMediaSessionState(isPlaying, playerStore.currentSong.value)
                if (isPlaying) {
                    startProgressUpdates()
                } else {
                    stopProgressUpdates()
                }
            }
        }
        serviceScope.launch {
            playerStore.currentSong.collectLatest {
                updateNotification()
                updateMediaSessionState(playerStore.isPlaying.value, it)
            }
        }
    }

    private suspend fun getAlbumArtBitmap(cover: String?, context: Context): Bitmap? {
        val serverConfigStore = getServerConfigStore(context)
        val currentServer = serverConfigStore.currentServer.value
        val serverBaseUrl = currentServer?.serverBaseUrl?.trimEnd('/')
        val url = resolveImageUrlWithSize(cover, serverBaseUrl, 256)
        if (url.isNullOrBlank()) return null
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        val result = loader.execute(request)
        return if (result is SuccessResult) {
            val drawable = result.image.asDrawable(
                resources = context.resources
            )
            if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                try {
                    val bitmap = createBitmap(drawable.intrinsicWidth.takeIf { it > 0 } ?: 256,
                        drawable.intrinsicHeight.takeIf { it > 0 } ?: 256)
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap
                } catch (_: Exception) {
                    null
                }
            }
        } else null
    }

    private suspend fun createNotificationWithArt(): Notification {
        val isPlaying = playerStore.isPlaying.value
        val currentSong = playerStore.currentSong.value
        val playPauseIcon = if (isPlaying) R.drawable.nmpausesolid else R.drawable.nmplaysolid
        val playPauseAction = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        val playPauseIntent = PendingIntent.getService(
            this, 0, Intent(this, MusicPlayerService::class.java).setAction(playPauseAction), PendingIntent.FLAG_IMMUTABLE
        )
        val nextIntent = PendingIntent.getService(
            this, 2, Intent(this, MusicPlayerService::class.java).setAction(ACTION_NEXT), PendingIntent.FLAG_IMMUTABLE
        )
        val prevIntent = PendingIntent.getService(
            this, 3, Intent(this, MusicPlayerService::class.java).setAction(ACTION_PREV), PendingIntent.FLAG_IMMUTABLE
        )
        val title = currentSong?.name ?: "Music Player"
        val artist = currentSong?.artistTrack?.firstOrNull()?.name ?: ""
        val cover = currentSong?.cover
        val artBitmap = if (cover != null) getAlbumArtBitmap(cover, this) else null

        // Get progress info from timeState
        val timeState = playerStore.timeState.value
        val duration = timeState.duration.toInt()
        val position = timeState.position.toInt()
        val showProgress = duration > 0 && position in 0..duration

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.ic_mediasession_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.nmprevioushalftone, "Previous", prevIntent)
            .addAction(playPauseIcon, if (isPlaying) "Pause" else "Play", playPauseIntent)
            .addAction(R.drawable.nmforwardhalftone, "Next", nextIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
        if (artBitmap != null) builder.setLargeIcon(artBitmap)
        if (showProgress) builder.setProgress(duration, position, false)
        return builder.build()
    }

    private fun updateNotification() {
        serviceScope.launch {
            val notification = createNotificationWithArt()
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MusicPlayerService", "onStartCommand called with action: ${intent?.action}")
        when (intent?.action) {
            ACTION_PLAY -> handlePlay()
            ACTION_PAUSE -> handlePause()
            ACTION_NEXT -> handleNext()
            ACTION_PREV -> handlePrev()
            ACTION_PLAY_TRACK -> {
                Log.w("MusicPlayerService", "ACTION_PLAY_TRACK not implemented: playlist/tracks logic is missing")
            }
            else -> {}
        }
        val notification = createNotification()
        Log.d("MusicPlayerService", "Posting initial notification (basic)")
        if (!isForegroundService()) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            Log.d("MusicPlayerService", "Called startForeground with notification")
        } else {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
            Log.d("MusicPlayerService", "Updated notification while already foreground")
        }
        serviceScope.launch {
            val notificationWithArt = createNotificationWithArt()
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notificationWithArt)
            Log.d("MusicPlayerService", "Updated notification with album art")
        }
        return START_STICKY
    }

    private fun isForegroundService(): Boolean {
        // This is a workaround: check if the service is already in foreground by checking if notification is active
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val activeNotifications = manager.activeNotifications
        return activeNotifications.any { it.id == NOTIFICATION_ID }
    }

    override fun onDestroy() {
        Log.d("MusicPlayerService", "onDestroy called")
        mediaSession.release()
        super.onDestroy()
    }

    private fun handlePlay() {
        Log.d("MusicPlayerService", "handlePlay called")
        playerStore.play()
    }

    private fun handlePause() {
        Log.d("MusicPlayerService", "handlePause called")
        playerStore.pause()
    }

    private fun handleNext() {
        Log.d("MusicPlayerService", "handleNext called")
        playerStore.next()
    }

    private fun handlePrev() {
        Log.d("MusicPlayerService", "handlePrev called")
        playerStore.previous()
    }

    private fun createNotification(): Notification {
        val isPlaying = playerStore.isPlaying.value
        val currentSong = playerStore.currentSong.value
        val playPauseIcon = if (isPlaying) R.drawable.nmpausesolid else R.drawable.nmplaysolid
        val playPauseAction = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        val playPauseIntent = PendingIntent.getService(
            this, 0, Intent(this, MusicPlayerService::class.java).setAction(playPauseAction), PendingIntent.FLAG_IMMUTABLE
        )
        val nextIntent = PendingIntent.getService(
            this, 2, Intent(this, MusicPlayerService::class.java).setAction(ACTION_NEXT), PendingIntent.FLAG_IMMUTABLE
        )
        val prevIntent = PendingIntent.getService(
            this, 3, Intent(this, MusicPlayerService::class.java).setAction(ACTION_PREV), PendingIntent.FLAG_IMMUTABLE
        )
        val title = currentSong?.name ?: "Music Player"
        val artist = currentSong?.artistTrack?.firstOrNull()?.name ?: ""
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.ic_mediasession_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.nmprevioussolid, "Previous", prevIntent)
            .addAction(playPauseIcon, if (isPlaying) "Pause" else "Play", playPauseIntent)
            .addAction(R.drawable.nmnexthalftone, "Next", nextIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
        // TODO: Add album art if available
        return builder.build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Player",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "Media playback controls"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    fun resolveImageUrlWithSize(cover: String?, serverBaseUrl: String?, size: Int = 256): String? {
        if (cover.isNullOrBlank()) return null
        val suffix = getSuffix()
        // Build the base URL (handle relative paths)
        val baseUrl = when {
            cover.startsWith("https://") && "fanart.tv" in cover -> {
                val proxy = "https://api$suffix.nomercy.tv/cors?url="
                proxy + cover
            }
            cover.startsWith("https://") -> cover
            cover.startsWith("/") && !serverBaseUrl.isNullOrBlank() -> {
                "$serverBaseUrl$cover"
            }
            else -> return null
        }
        // Always append the query string for size/type/aspect_ratio
        val separator = if (baseUrl.contains("?")) "&" else "?"
        return baseUrl + separator + "width=$size&type=png&aspect_ratio=1"
    }


    companion object {
        private const val ACTION_PLAY = "tv.nomercy.app.action.PLAY"
        private const val ACTION_PAUSE = "tv.nomercy.app.action.PAUSE"
        private const val ACTION_NEXT = "tv.nomercy.app.action.NEXT"
        private const val ACTION_PREV = "tv.nomercy.app.action.PREV"
        private const val ACTION_PLAY_TRACK = "tv.nomercy.app.action.PLAY_TRACK"
    }
}
