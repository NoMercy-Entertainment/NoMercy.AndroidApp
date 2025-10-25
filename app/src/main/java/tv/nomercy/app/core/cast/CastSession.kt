package tv.nomercy.app.core.cast

import kotlinx.coroutines.flow.Flow

/** Represents a paired controller<->receiver session. */
interface CastSession {
    val id: String
    val role: CastRole

    /** Outgoing messages from this device. */
    suspend fun send(message: CastMessage)

    /** Incoming messages to this device (from the other peer). */
    val incoming: Flow<CastMessage>

    suspend fun close(cause: Throwable? = null)
}
