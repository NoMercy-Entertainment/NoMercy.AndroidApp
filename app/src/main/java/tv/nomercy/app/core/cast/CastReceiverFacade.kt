package tv.nomercy.app.core.cast

/**
 * Entry point for the TV app to receive commands from a mobile controller.
 *
 * Placeholder wiring that uses a no-op signaling client.
 */
class CastReceiverFacade(
    private val receiver: CastReceiver,
    private val signaling: CastSignalingClient = CastNoop()
) {
    suspend fun start() {
        signaling.start(CastRole.Receiver)
        // In a real implementation, collect signaling.incoming and route to receiver
    }

    suspend fun onMessage(msg: CastMessage) {
        when (msg) {
            is Control -> receiver.onControl(msg)
            is PairRequest -> receiver.onPair(msg)
            is Discover -> receiver.onDiscover(msg)
            else -> { /* ignore */ }
        }
    }

    suspend fun stop() {
        signaling.stop()
    }
}
