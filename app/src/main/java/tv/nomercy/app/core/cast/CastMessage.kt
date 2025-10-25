package tv.nomercy.app.core.cast

/**
 * Minimal protocol for mobile->TV control via our own TV app, not Google Cast.
 * This is a transport-agnostic message model. Signaling may be via server WebSocket
 * or LAN discovery in future iterations. Keep messages small and additive-only.
 */
sealed interface CastMessage {
    val ts: Long
}

// Discovery and pairing
data class Discover(val controllerId: String, override val ts: Long = System.currentTimeMillis()) : CastMessage

data class Advertise(val tvId: String, val name: String, override val ts: Long = System.currentTimeMillis()) : CastMessage

data class PairRequest(
    val controllerId: String,
    val tvId: String,
    val pin: String?,
    override val ts: Long = System.currentTimeMillis()
) : CastMessage

data class PairResult(
    val ok: Boolean,
    val reason: String? = null,
    val sessionId: String? = null,
    override val ts: Long = System.currentTimeMillis()
) : CastMessage

// Control commands
sealed interface Control : CastMessage

data class Play(val mediaId: String, val positionMs: Long? = null, override val ts: Long = System.currentTimeMillis()) : Control

data class Pause(override val ts: Long = System.currentTimeMillis()) : Control

data class SeekTo(val positionMs: Long, override val ts: Long = System.currentTimeMillis()) : Control

data class SetVolume(val volume: Float, override val ts: Long = System.currentTimeMillis()) : Control

data class Navigate(val route: String, override val ts: Long = System.currentTimeMillis()) : Control

// State updates
sealed interface State : CastMessage

data class AppState(
    val route: String?,
    val mediaId: String?,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long?,
    override val ts: Long = System.currentTimeMillis()
) : State
