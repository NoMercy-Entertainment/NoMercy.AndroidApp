package tv.nomercy.app.shared.api.services

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import tv.nomercy.app.shared.models.ApiResponse
import tv.nomercy.app.shared.models.AppConfig
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.InfoResponse
import tv.nomercy.app.shared.models.Library
import tv.nomercy.app.shared.models.MusicList
import tv.nomercy.app.shared.models.PermissionsResponse
import tv.nomercy.app.shared.models.Server
import tv.nomercy.app.shared.models.UserProfile

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
    suspend fun getUserServers(): Response<ApiResponse<List<Server>>>
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

    @GET("home") // raw json
    suspend fun getMobileHome(): Response<ResponseBody>

    @GET("home/tv") // raw json
    suspend fun getTvHome(): Response<ResponseBody>

    /**
     * Get libraries from the user's server
     */
    @GET("libraries")
    suspend fun getLibraries(): Response<ApiResponse<List<Library>>>

    @GET("libraries/mobile")
    suspend fun getLibraryList(): Response<ApiResponse<List<Component>>>

    @GET("libraries/tv")
    suspend fun getTvLibraryList(): Response<ApiResponse<List<Component>>>

    /**
     * Get media items from a specific library
     */
    @GET("{link}")
    suspend fun getLibraryItems(
        @Path(value = "link", encoded = true) link: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<Component>>>



    /**
     * Get media items from a specific library
     */
    @GET("{type}/{id}")
    suspend fun getInfo(
        @Path(value = "type", encoded = true) type: String,
        @Path("id", encoded = true) id: String,
    ): Response<ApiResponse<InfoResponse>>


    /**
     * Get media items from a specific library
     */
    @GET("music/{type}/{id}")
    suspend fun getList(
        @Path(value = "type", encoded = true) type: String,
        @Path("id", encoded = true) id: String,
    ): Response<ApiResponse<MusicList>>

     @GET("{path}")
     suspend fun getComponentResponse(
         @Path("path", encoded = true) path: String
     ): Response<ApiResponse<List<Component>>>


    @GET("music/start") // raw json
    suspend fun getMusicStart(): Response<ResponseBody>

    /**
     * Get libraries from the user's server
     */
    @GET("music/{type}/{char}")
    suspend fun getCards(
        @Path("type", encoded = true) type: String,
        @Path("char") char: Char
    ): Response<ApiResponse<List<Component>>>

    @GET("{link}")
    suspend fun getCardItems(
        @Path(value = "link", encoded = true) link: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<Component>>>


    @GET("music/search")
    suspend fun searchMusic(
        @Query("query") query: String
    ): Response<ResponseBody>

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
