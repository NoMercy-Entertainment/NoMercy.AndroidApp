package tv.nomercy.app.api.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tv.nomercy.app.api.ServerApiClient
import tv.nomercy.app.api.models.Component
import tv.nomercy.app.api.models.Library
import tv.nomercy.app.api.models.MediaItem
import tv.nomercy.app.api.services.ServerApiService
import tv.nomercy.app.auth.AuthService
import tv.nomercy.app.store.AuthStore

/**
 * Repository for managing library data from the server
 * This class specifically handles SERVER API calls for library data
 */
class LibraryRepository(
    private val context: Context,
    private val authStore: AuthStore,
    private val authService: AuthService
) {
    private val apiServiceCache = mutableMapOf<String, ServerApiService>()

    private fun createServerApiService(serverUrl: String): ServerApiService {
        return apiServiceCache.getOrPut(serverUrl) {
            val serverApiClient = ServerApiClient.create(serverUrl, context, authService, authStore)
            serverApiClient.createService<ServerApiService>()
        }
    }

    suspend fun getLibraries(serverUrl: String): Flow<Result<List<Library>>> = flow {
        try {
            val serverApiService = createServerApiService(serverUrl)

            val response = serverApiService.getLibraries()

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.data != null) {
                    emit(Result.success(apiResponse.data))
                } else {
                    println("DEBUG LibraryRepository: API response body or data is null")
                    emit(Result.failure(Exception("No library data received")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG LibraryRepository: API call failed: ${response.message()}, body: $errorBody")
                emit(Result.failure(Exception("Failed to fetch libraries: ${response.message()}")))
            }
        } catch (e: Exception) {
            println("DEBUG LibraryRepository: Exception in getLibraries: ${e.message}")
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

    suspend fun getLibraryItems(
        serverUrl: String,
        link: String,
        page: Int = 0,
        limit: Int = 20
    ): Flow<Result<List<Component<MediaItem>>>> = flow {
        try {
            val serverApiService = createServerApiService(serverUrl)
            val response = serverApiService.getLibraryItems(link, page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                println("DEBUG LibraryRepository: API response: $apiResponse")
                if (apiResponse?.data != null) {
                    emit(Result.success(apiResponse.data))
                } else {
                    emit(Result.failure(Exception("No media items received")))
                }
            } else {
                emit(Result.failure(Exception("Failed to fetch library items: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

}
