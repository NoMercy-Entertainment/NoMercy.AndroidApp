package tv.nomercy.app.shared.api.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import tv.nomercy.app.shared.api.ServerApiClient
import tv.nomercy.app.shared.api.services.AuthService
import tv.nomercy.app.shared.api.services.ServerApiService
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.HomeResponse
import tv.nomercy.app.shared.models.Library
import tv.nomercy.app.shared.models.MediaItem
import tv.nomercy.app.shared.stores.AuthStore

class LibraryRepository(
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

    fun getLibraries(serverUrl: String): Flow<Result<List<Library>>> = flow {
        try {
            val service = createServerApiService(serverUrl)
            val response = service.getLibraries()

            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                emit(Result.success(data))
            } else {
                emit(Result.failure(Exception("Failed to fetch libraries: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getLibraryItems(
        serverUrl: String,
        link: String,
        page: Int = 0,
        limit: Int = 20
    ): Flow<Result<List<Component<MediaItem>>>> = flow {
        try {
            val service = createServerApiService(serverUrl)
            val response = service.getLibraryItems(link, page, limit)

            val parsed = response.body()?.data ?: emptyList()
            emit(Result.success(parsed))
        } catch (e: Exception) {
            println(e.message)
            emit(Result.failure(e))
        }
    }
}
