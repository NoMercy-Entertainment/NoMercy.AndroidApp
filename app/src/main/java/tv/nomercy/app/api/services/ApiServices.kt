package tv.nomercy.app.api.services

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import tv.nomercy.app.api.models.ApiResponse
import tv.nomercy.app.api.models.AppConfig
import tv.nomercy.app.api.models.PermissionsResponse
import tv.nomercy.app.api.models.UserProfile
import tv.nomercy.app.api.models.Library
import tv.nomercy.app.api.models.MediaItem
import tv.nomercy.app.api.models.LibraryStats

/**
 * Domain API service for NoMercy TV API endpoints
 */
interface DomainApiService {

    /**
     * Get app configuration including user info, servers, and permissions
     * Equivalent to the /app_config endpoint from the TypeScript version
     */
    @GET("app_config")
    suspend fun getAppConfig(): Response<ApiResponse<AppConfig>>

    /**
     * Get user profile information
     */
    @GET("user/profile")
    suspend fun getUserProfile(): Response<ApiResponse<UserProfile>>

    /**
     * Get user's servers
     */
    @GET("user/servers")
    suspend fun getUserServers(): Response<ApiResponse<List<tv.nomercy.app.api.models.Server>>>
}

/**
 * Server API service for user's specific server endpoints
 */
interface ServerApiService {

    /**
     * Get server status and information
     */
    @GET("status")
    suspend fun getServerStatus(): Response<ApiResponse<Map<String, Any>>>

    /**
     * Get server permissions for the current user
     */
    @GET("permissions")
    suspend fun getServerPermissions(): Response<ApiResponse<PermissionsResponse>>

    /**
     * Get libraries from the user's server
     */
    @GET("libraries")
    suspend fun getLibraries(): Response<ApiResponse<List<Library>>>

    /**
     * Get media items from a specific library
     */
    @GET("libraries/{libraryId}/items")
    suspend fun getLibraryItems(
        @Path("libraryId") libraryId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<MediaItem>>>

    /**
     * Get library statistics
     */
    @GET("libraries/{libraryId}/stats")
    suspend fun getLibraryStats(
        @Path("libraryId") libraryId: String
    ): Response<ApiResponse<LibraryStats>>
}

/**
 * Generic API service interface
 */
interface GenericApiService {

    /**
     * Generic GET request
     */
    @GET("{endpoint}")
    suspend fun get(@Path("endpoint") endpoint: String): Response<ApiResponse<Any>>

    /**
     * Generic GET request with query parameters
     */
    @GET("{endpoint}")
    suspend fun getWithQuery(
        @Path("endpoint") endpoint: String,
        @Query("query") queryParams: Map<String, String>
    ): Response<ApiResponse<Any>>
}
