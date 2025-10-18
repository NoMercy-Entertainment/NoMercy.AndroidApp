package tv.nomercy.app.core.cast

/** API for the TV side to handle incoming commands. */
interface CastReceiver {
    suspend fun onControl(command: Control)
    suspend fun onPair(request: PairRequest)
    suspend fun onDiscover(discover: Discover)
}
