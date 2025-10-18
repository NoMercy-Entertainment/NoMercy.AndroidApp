package tv.nomercy.app.shared.repositories

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import tv.nomercy.app.shared.api.KeycloakConfig
import tv.nomercy.app.shared.api.ServerApiClient
import tv.nomercy.app.shared.api.parseComponentsParallel
import tv.nomercy.app.shared.api.services.AuthService
import tv.nomercy.app.shared.api.services.ServerApiService
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.stores.AuthStore

class HomeRepository(
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

    fun fetch(serverUrl: String): Flow<Result<List<Component>>> =
        if (KeycloakConfig.isTv(context)) fetchTvHome(serverUrl)
        else fetchMobileHome(serverUrl)


    fun fetchMobileHome(serverUrl: String): Flow<Result<List<Component>>> = flow {
        try {
            val service = createServerApiService(serverUrl)

            val response = service.getMobileHome()
            val jsonString = response.body()?.string() ?: throw Exception("Empty response")

            val parsed = withContext(Dispatchers.Default) {
                parseComponentsParallel(jsonString)
            }

            emit(Result.success(parsed))
        } catch (e: Exception) {
            println(e.message)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun fetchTvHome(serverUrl: String): Flow<Result<List<Component>>> = flow {
        try {
            val service = createServerApiService(serverUrl)

            val response = service.getTvHome()
            val jsonString = response.body()?.string() ?: throw Exception("Empty response")

            val parsed = withContext(Dispatchers.Default) {
                parseComponentsParallel(jsonString)
            }

            emit(Result.success(parsed))
        } catch (e: Exception) {
            println(e.message)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
