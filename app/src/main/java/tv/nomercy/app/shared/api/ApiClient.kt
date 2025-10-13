package tv.nomercy.app.shared.api

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import tv.nomercy.app.shared.api.services.AuthService
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.ComponentData
import tv.nomercy.app.shared.models.NMCardProps
import tv.nomercy.app.shared.models.NMHomeCardProps
import tv.nomercy.app.shared.models.NMCarouselProps
import tv.nomercy.app.shared.models.NMContainerProps
import tv.nomercy.app.shared.models.NMGridProps
import tv.nomercy.app.shared.stores.AuthStore
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

    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun buildRequestWithHeaders(original: Request, token: String?): Request {
        val locale = context.resources.configuration.locales.get(0)
        return original.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept-Language", locale.language)
            .apply {
                token?.let { addHeader("Authorization", "Bearer $it") }
            }
            .build()
    }

    private val authInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            // Get token from shared AuthStore
            val token = runBlocking {
                try {
                    authStore.accessToken.first()
                } catch (e: Exception) {
                    null
                }
            }

            val response = chain.proceed(buildRequestWithHeaders(originalRequest, token).newBuilder().build())

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
                        return refreshedToken.let {
                            chain.proceed(buildRequestWithHeaders(originalRequest, it))
                        }
                    }
                } else {
                    authStore.clearData()
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
        .addConverterFactory(jsonConfig.asConverterFactory("application/json".toMediaType()))
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


suspend fun parseComponentsParallel(jsonString: String): List<Component> = coroutineScope {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    val root = json.parseToJsonElement(jsonString).jsonObject
    val dataArray = root["data"]?.jsonArray ?: JsonArray(emptyList())

    // Decode each component in parallel
    dataArray.map { element ->
        async(Dispatchers.Default) {
            json.decodeFromJsonElement(Component.serializer(), element)
        }
    }.awaitAll()
}