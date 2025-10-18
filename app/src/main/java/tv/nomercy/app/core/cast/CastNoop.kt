package tv.nomercy.app.core.cast

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Default no-op signaling implementation used until a real transport is wired.
 * Safe to keep in production; it does nothing.
 */
class CastNoop : CastSignalingClient {
    private val _incoming = MutableSharedFlow<CastMessage>(extraBufferCapacity = 16)
    private val _discoveries = MutableSharedFlow<Advertise>(extraBufferCapacity = 16)

    override val discoveries: Flow<Advertise> = _discoveries.asSharedFlow()
    override val incoming: Flow<CastMessage> = _incoming.asSharedFlow()

    override suspend fun start(role: CastRole) { /* no-op */ }
    override suspend fun send(message: CastMessage) { /* no-op */ }
    override suspend fun stop() { /* no-op */ }
}
