package tv.nomercy.app.shared.repositories

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import tv.nomercy.app.shared.api.KeycloakConfig
import tv.nomercy.app.shared.api.ServerApiClient
import tv.nomercy.app.shared.api.services.AuthService
import tv.nomercy.app.shared.api.services.ServerApiService
import tv.nomercy.app.shared.models.Component
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

    fun getLibraries(serverUrl: String): Flow<Result<List<Component>>> =
        if (KeycloakConfig.isTv(context)) getTvLibraries(serverUrl)
        else getMobileLibraries(serverUrl)


    fun getMobileLibraries(serverUrl: String): Flow<Result<List<Component>>> = flow {
        try {
            val service = createServerApiService(serverUrl)

            val parsed = withContext(Dispatchers.Default) {
                val response = service.getLibraryList()
                response.body()?.data ?: emptyList()
            }

            emit(Result.success(parsed))
        } catch (e: Exception) {
            println(e.message)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getTvLibraries(serverUrl: String): Flow<Result<List<Component>>> = flow {
        try {
            val service = createServerApiService(serverUrl)

            val parsed = withContext(Dispatchers.Default) {
                val response = service.getTvLibraryList()
                response.body()?.data ?: emptyList()
            }

            emit(Result.success(parsed))
        } catch (e: Exception) {
            println(e.message)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

}