package tv.nomercy.app.core.cast

import kotlinx.coroutines.flow.Flow

/**
 * Abstract signaling transport.
 * Initial version will be a server-relayed WebSocket channel (controller <-> server <-> tv).
 * Future: LAN discovery (mDNS/SSDP) + direct WebSocket/UDP.
 */
interface CastSignalingClient {
    /** Emits discovered receivers (TVs) on the network/account. */
    val discoveries: Flow<Advertise>

    /** Connect and start advertising or discovering based on role. */
    suspend fun start(role: CastRole)

    /** Send a signaling/control message. */
    suspend fun send(message: CastMessage)

    /** All incoming messages from the signaling layer. */
    val incoming: Flow<CastMessage>

    suspend fun stop()
}
