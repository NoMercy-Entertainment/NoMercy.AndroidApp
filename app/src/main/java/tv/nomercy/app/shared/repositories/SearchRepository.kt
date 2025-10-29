package tv.nomercy.app.shared.repositories

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import tv.nomercy.app.shared.api.ServerApiClient
import tv.nomercy.app.shared.api.parseComponentsParallel
import tv.nomercy.app.shared.api.services.AuthService
import tv.nomercy.app.shared.api.services.ServerApiService
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.SearchResultElement
import tv.nomercy.app.shared.stores.AuthStore
import tv.nomercy.app.shared.stores.GlobalStores
import java.util.Locale

class SearchRepository(
    private val context: Context,
    private val authStore: AuthStore,
    private val authService: AuthService
) {
    val appConfigStore = GlobalStores.getAppConfigStore(context)

    private val apiServiceCache = mutableMapOf<String, ServerApiService>()
    val locale: Locale = context.resources.configuration.locales.get(0)

    private fun createServerApiService(serverUrl: String): ServerApiService {
        return apiServiceCache.getOrPut(serverUrl) {
            val client = ServerApiClient.create(serverUrl, context, authService, authStore)
            client.createService<ServerApiService>()
        }
    }

    fun searchMusic(serverUrl: String, query: String): Flow<Result<List<Component>>> = flow {
        try {
            val service = createServerApiService(serverUrl)
            val response = service.searchMusic(query)
            val jsonString = response.body()?.string() ?: throw Exception("Empty music response")

            Log.d("searchVideo", jsonString)

            val parsed = withContext(Dispatchers.Default) {
                parseComponentsParallel(jsonString)
            }

            emit(Result.success(parsed))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun searchVideo(query: String): Flow<Result<List<SearchResultElement>>> = flow {
        try {
            val url = "https://api.themoviedb.org/3/search/multi?api_key=${appConfigStore.tmdbApiKey}&language=${locale.language}&query=${query}&include_adult=false"
            val response = OkHttpClient().newCall(Request.Builder().url(url).build()).execute()
            val jsonString = response.body.string()

            Log.d("searchVideo", jsonString)

            val parsed = withContext(Dispatchers.Default) {
                parseTmdbResults(jsonString)
            }

            emit(Result.success(parsed))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}

fun parseTmdbResults(jsonString: String): List<SearchResultElement> {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    val root = json.parseToJsonElement(jsonString).jsonObject
    val dataArray = root["results"]?.jsonArray ?: JsonArray(emptyList())

    val list = mutableListOf<SearchResultElement>()

    dataArray.map { element ->
        try {
            val item = json.decodeFromJsonElement(SearchResultElement.serializer(), element)
            list.add(item)
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to parse MediaItem: ${e.message}")
        }
    }

    return list
}