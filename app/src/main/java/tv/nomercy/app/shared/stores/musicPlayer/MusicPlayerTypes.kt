package tv.nomercy.app.shared.stores.musicPlayer

// PlaylistItem is located at java/tv/nomercy/app/shared/models/PlaylistItem.kt

data class TimeState(
    val buffered: Long = 0,
    val duration: Long = 0,
    val percentage: Float = 0f,
    val position: Long = 0,
    val remaining: Long = 0,
)

enum class PlayerState {
    IDLE,       // Ready but not playing
    LOADING,    // Loading audio file
    PLAYING,    // Currently playing
    PAUSED,     // Paused
    STOPPED,    // Stopped
    ENDED,      // Track ended
    BUFFERING,  // Buffering data
    ERROR       // Error occurred
}

enum class VolumeState {
    MUTED,
    UNMUTED
}

enum class RepeatState {
    OFF,
    ONE,
    ALL
}

data class MusicPlayerConfig(
    val baseUrl: String,
    val accessToken: String?,
    val disableAutoPlayback: Boolean = false,
    val crossFadeSteps: Int = 10,
    val fadeDuration: Int = 3,
    val prefetchLeeway: Int = 20,
)
