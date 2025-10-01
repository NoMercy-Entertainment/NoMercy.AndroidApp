package tv.nomercy.app.api.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tv.nomercy.app.api.ServerApiClient
import tv.nomercy.app.api.models.Library
import tv.nomercy.app.api.models.LibraryStats
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

    /**
     * Create server API client dynamically based on current server
     */
    private fun createServerApiService(serverUrl: String): ServerApiService {
        val serverApiClient = ServerApiClient.create(serverUrl, context, authService, authStore)
        return serverApiClient.createService<ServerApiService>()
    }

    /**
     * Fetch libraries from the specified server
     */
    suspend fun getLibraries(serverUrl: String): Flow<Result<List<Library>>> = flow {
        try {
            val serverApiService = createServerApiService(serverUrl)
            val response = serverApiService.getLibraries()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.data != null) {
                    emit(Result.success(apiResponse.data))
                } else {
                    emit(Result.failure(Exception("No library data received")))
                }
            } else {
                emit(Result.failure(Exception("Failed to fetch libraries: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Fetch media items from a specific library on the specified server
     */
    suspend fun getLibraryItems(
        serverUrl: String,
        libraryId: String,
        page: Int = 1,
        limit: Int = 20
    ): Flow<Result<List<MediaItem>>> = flow {
        try {
            val serverApiService = createServerApiService(serverUrl)
            val response = serverApiService.getLibraryItems(libraryId, page, limit)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.data != null) {
                    emit(Result.success(apiResponse.data))
                } else {
                    emit(Result.failure(Exception("No media items received")))
                }
            } else {
                emit(Result.failure(Exception("Failed to fetch library items: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Fetch library statistics from the specified server
     */
    suspend fun getLibraryStats(serverUrl: String, libraryId: String): Flow<Result<LibraryStats>> = flow {
        try {
            val serverApiService = createServerApiService(serverUrl)
            val response = serverApiService.getLibraryStats(libraryId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.data != null) {
                    emit(Result.success(apiResponse.data))
                } else {
                    emit(Result.failure(Exception("No library stats received")))
                }
            } else {
                emit(Result.failure(Exception("Failed to fetch library stats: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Get libraries filtered by type from the specified server
     */
    suspend fun getLibrariesByType(serverUrl: String, type: String): Flow<Result<List<Library>>> = flow {
        try {
            getLibraries(serverUrl).collect { result ->
                result.fold(
                    onSuccess = { libraries ->
                        val filteredLibraries = libraries.filter { it.type.equals(type, ignoreCase = true) }
                        emit(Result.success(filteredLibraries))
                    },
                    onFailure = { error ->
                        emit(Result.failure(error))
                    }
                )
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
