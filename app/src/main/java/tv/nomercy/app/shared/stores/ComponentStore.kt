package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import tv.nomercy.app.shared.api.ServerApiClient
import tv.nomercy.app.shared.api.services.ServerApiService
import tv.nomercy.app.shared.models.Component

class ComponentStore(
    private val context: Context,
    private val authStore: AuthStore,
    private val appConfigStore: AppConfigStore
) {
    private val authService = GlobalStores.getAuthService(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _componentMap = MutableStateFlow<Map<String, List<Component<*>>>>(emptyMap())
    val componentMap = _componentMap.asStateFlow()

    private val apiServiceCache = mutableMapOf<String, ServerApiService>()

    private fun createServerApiService(serverUrl: String): ServerApiService {
        return apiServiceCache.getOrPut(serverUrl) {
            val serverApiClient = ServerApiClient.create(serverUrl, context, authService, authStore)
            serverApiClient.createService<ServerApiService>()
        }
    }

    suspend fun fetchComponents(path: String, serverUrl: String) {
        try {
            val service = createServerApiService(serverUrl)
            val response = service.getComponentResponse(path)

            if (response.isSuccessful) {
                val components = response.body()?.data ?: emptyList()
                _componentMap.update { it + (path to components) }
            } else {
                println("❌ Server error for $path: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            println("❌ Exception while fetching $path: ${e.message}")
        }
    }

    fun getComponents(path: String): List<Component<*>> {
        return componentMap.value[path] ?: emptyList()
    }

    fun hasComponents(path: String): Boolean {
        return componentMap.value.containsKey(path)
    }
}