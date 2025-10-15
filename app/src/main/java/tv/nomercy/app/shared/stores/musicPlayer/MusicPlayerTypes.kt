package tv.nomercy.app.shared.stores.musicPlayer

/**
 * Types and configuration for the music player module.
 */
// PlaylistItem is located at java/tv/nomercy/app/shared/models/PlaylistItem.kt

/**
 * Represents the current playback time state.
 */
data class TimeState(
    val buffered: Long = 0,
    val duration: Long = 0,
    val percentage: Float = 0f,
    val position: Long = 0,
    val remaining: Long = 0,
)

/**
 * Player state for the music player.
 */
enum class PlayerState {
    IDLE,       // Ready but not playing
    LOADING,    // Loading audio file
    READY,
    PLAYING,    // Currently playing
    PAUSED,     // Paused
    STOPPED,    // Stopped
    ENDED,      // Track ended
    BUFFERING,  // Buffering data
    ERROR       // Error occurred
}

/**
 * Volume state for the music player.
 */
enum class VolumeState {
    MUTED,
    UNMUTED
}

/**
 * Repeat state for the music player.
 */
enum class RepeatState {
    OFF,
    ONE,
    ALL
}

/**
 * Configuration for the music player.
 */
data class MusicPlayerConfig(
    val baseUrl: String,
    val accessToken: String?,
    val disableAutoPlayback: Boolean = false,
    val crossFadeSteps: Int = 10,
    val fadeDuration: Int = 3,
    val prefetchLeeway: Int = 20,
)
