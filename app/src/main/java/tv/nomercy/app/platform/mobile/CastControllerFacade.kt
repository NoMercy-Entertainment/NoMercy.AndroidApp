package tv.nomercy.app.platform.mobile

import kotlinx.coroutines.flow.Flow
import tv.nomercy.app.core.cast.Advertise
import tv.nomercy.app.core.cast.CastMessage
import tv.nomercy.app.core.cast.CastNoop
import tv.nomercy.app.core.cast.CastRole
import tv.nomercy.app.core.cast.CastSignalingClient

/**
 * Entry point for the Mobile app to control the TV app via our custom cast protocol.
 *
 * This is a placeholder facade that uses a no-op signaling client. Replace the
 * CastNoop with a real implementation (e.g., server WebSocket) and wire into
 * mobile UI when ready.
 */
class CastControllerFacade(
    private val signaling: CastSignalingClient = CastNoop()
) {
    val discoveries: Flow<Advertise> get() = signaling.discoveries

    suspend fun startDiscovery() {
        signaling.start(CastRole.Controller)
    }

    suspend fun connectToTv(tvId: String) {
        // In a real implementation, emit a PairRequest, then await PairResult
        // and promote to a CastSession. Kept minimal here.
    }

    suspend fun send(message: CastMessage) {
        signaling.send(message)
    }

    suspend fun stop() {
        signaling.stop()
    }
}
