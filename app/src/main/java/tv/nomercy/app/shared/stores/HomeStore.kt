package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.repositories.HomeRepository
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMCardProps

class HomeStore(
    context: Context,
    authStore: AuthStore,
    private val serverConfigStore: ServerConfigStore,
) {
    private val authService = GlobalStores.getAuthService(context)
    private val repository = HomeRepository(context, authStore, authService)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _homeData = MutableStateFlow<List<Component<NMCardProps>>>(emptyList())
    val homeData: StateFlow<List<Component<NMCardProps>>> = _homeData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun getServerUrl(): String? = serverConfigStore.currentServer.value?.serverApiUrl

    fun fetch(force: Boolean = false) {
        val serverUrl = getServerUrl() ?: run {
            _error.value = "No server selected"
            return
        }

        if (!force && _homeData.value.isNotEmpty()) return

        scope.launch {
            _isLoading.value = true
            _error.value = null

            repository.fetch(serverUrl).collect { result ->
                result.fold(
                    onSuccess = { items -> _homeData.value = items },
                    onFailure = { _error.value = it.message ?: "Failed to fetch home data" }
                )
                _isLoading.value = false
            }
        }
    }


    fun clearData() {
        _homeData.value = emptyList()
        _error.value = null
        _isLoading.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
