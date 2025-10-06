package tv.nomercy.app.shared.api.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tv.nomercy.app.shared.api.ServerApiClient
import tv.nomercy.app.shared.api.services.AuthService
import tv.nomercy.app.shared.api.services.ServerApiService
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.ComponentData
import tv.nomercy.app.shared.models.NMCardProps
import tv.nomercy.app.shared.stores.AuthStore

class LibrariesRepository(
    private val context: Context,
    private val authStore: AuthStore,
    private val authService: AuthService
) {
    private val apiServiceCache = mutableMapOf<String, ServerApiService>()

    private fun createServerApiService(serverUrl: String): ServerApiService {
        return apiServiceCache.getOrPut(serverUrl) {
            val client = ServerApiClient.create(serverUrl, context, authService, authStore)
            client.createService<ServerApiService>()
        }
    }

    fun getHomeData(
        serverUrl: String,
    ): Flow<Result<List<Component<NMCardProps>>>> = flow {
        try {
            val service = createServerApiService(serverUrl)
            val response = service.getHome()

            val parsed = response.body()?.data ?: emptyList()
            emit(Result.success(parsed))
        } catch (e: Exception) {
            println(e.message)
            emit(Result.failure(e))
        }
    }
}