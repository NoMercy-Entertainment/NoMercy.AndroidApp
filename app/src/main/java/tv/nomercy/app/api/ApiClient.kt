package tv.nomercy.app.api

import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tv.nomercy.app.auth.AuthService
import tv.nomercy.app.store.AuthStore
import java.util.concurrent.TimeUnit

/**
 * Base API client with authentication handling
 */
open class BaseApiClient(
    private val baseUrl: String,
    private val context: Context,
    private val authService: AuthService,
    private val authStore: AuthStore, // Accept shared AuthStore instead of creating new one
    private val timeout: Long = 30L
) {

    private val authInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val userLocale = context.resources.configuration.locales.get(0)

            // Add standard headers
            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept-Language", userLocale.language)

            // Get token from shared AuthStore
            val token = runBlocking {
                try {
                    authStore.accessToken.first()
                } catch (e: Exception) {
                    null
                }
            }

            // Add authorization header if token exists
            token?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            val response = chain.proceed(requestBuilder.build())

            // Handle 401 Unauthorized by attempting token refresh
            if (response.code == 401 && token != null) {
                response.close()

                val refreshSuccess = runBlocking {
                    try {
                        authService.refreshToken()
                    } catch (e: Exception) {
                        false
                    }
                }

                if (refreshSuccess) {
                    // Get the new token after refresh
                    val newToken = runBlocking {
                        try {
                            authStore.accessToken.first()
                        } catch (e: Exception) {
                            null
                        }
                    }

                    newToken?.let { refreshedToken ->
                        // Retry the request with the new token
                        val retryRequestBuilder = originalRequest.newBuilder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", "Bearer $refreshedToken")
                            .addHeader("Accept-Language", userLocale.language)

                        return chain.proceed(retryRequestBuilder.build())
                    }
                } else {
                    authStore.clearAuth()
                }
            }

            return response
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(timeout, TimeUnit.SECONDS)
        .readTimeout(timeout, TimeUnit.SECONDS)
        .writeTimeout(timeout, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    inline fun <reified T> createService(): T {
        return retrofit.create(T::class.java)
    }
}

/**
 * Domain API client for NoMercy TV API
 */
class DomainApiClient(
    context: Context,
    authService: AuthService,
    authStore: AuthStore, // Accept shared AuthStore
    timeout: Long = 30L
) : BaseApiClient(
    baseUrl = "https://api-dev.nomercy.tv/v1/", // Will be made configurable later
    context = context,
    authService = authService,
    authStore = authStore, // Use shared AuthStore
    timeout = timeout
)

/**
 * Server API client for user's specific server
 */
class ServerApiClient(
    private val serverUrl: String,
    context: Context,
    authService: AuthService,
    authStore: AuthStore, // Accept shared AuthStore
    timeout: Long = 30L
) : BaseApiClient(
    baseUrl = serverUrl,
    context = context,
    authService = authService,
    authStore = authStore, // Use shared AuthStore
    timeout = timeout
) {
    companion object {
        fun create(
            serverUrl: String?,
            context: Context,
            authService: AuthService,
            authStore: AuthStore, // Accept shared AuthStore
            timeout: Long = 30L
        ): ServerApiClient {
            if (serverUrl.isNullOrBlank()) {
                throw IllegalArgumentException("Server URL cannot be null or empty")
            }
            return ServerApiClient(serverUrl, context, authService, authStore, timeout)
        }
    }
}

/**
 * Generic API client for any base URL
 */
class GenericApiClient(
    baseUrl: String,
    context: Context,
    authService: AuthService,
    authStore: AuthStore, // Accept shared AuthStore
    timeout: Long = 30L
) : BaseApiClient(
    baseUrl = baseUrl,
    context = context,
    authService = authService,
    authStore = authStore, // Use shared AuthStore
    timeout = timeout
)
