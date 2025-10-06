package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.api.repository.HomeRepository
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.ComponentData
import tv.nomercy.app.shared.models.NMCardProps

class LibrariesStore(
    context: Context,
    authStore: AuthStore,
    private val serverConfigStore: ServerConfigStore,
) {
    private val authService = GlobalStores.getAuthService(context)
    private val repository = HomeRepository(context, authStore, authService)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _librariesData = MutableStateFlow<List<Component<NMCardProps>>>(emptyList())
    val librariesData: StateFlow<List<Component<NMCardProps>>> = _librariesData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun getServerUrl(): String? = serverConfigStore.currentServer.value?.serverApiUrl

    init {
        fetch()
    }

    fun fetch(force: Boolean = false) {
        val serverUrl = getServerUrl() ?: run {
            _error.value = "No server selected"
            return
        }

        if (!force && _librariesData.value.isNotEmpty()) return

        scope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getHomeData(serverUrl).collect { result ->
                result.fold(
                    onSuccess = { items -> _librariesData.value = items },
                    onFailure = { _error.value = it.message ?: "Failed to fetch libraries data" }
                )
                _isLoading.value = false
            }
        }
    }


    fun clearData() {
        _librariesData.value = emptyList()
        _error.value = null
        _isLoading.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
