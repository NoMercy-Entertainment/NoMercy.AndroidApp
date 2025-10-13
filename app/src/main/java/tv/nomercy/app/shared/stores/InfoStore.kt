package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.models.InfoResponse
import tv.nomercy.app.shared.repositories.InfoRepository

class InfoStore(
    context: Context,
    authStore: AuthStore,
    private val serverConfigStore: ServerConfigStore,
) {
    private val authService = GlobalStores.getAuthService(context)
    private val repository = InfoRepository(context, authStore, authService)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _type = MutableStateFlow<String?>(null)
    val type: StateFlow<String?> = _type.asStateFlow()

    private val _id = MutableStateFlow<String?>(null)
    val id: StateFlow<String?> = _id.asStateFlow()

    private val _infoData = MutableStateFlow<InfoResponse?>(null)
    val infoData: StateFlow<InfoResponse?> = _infoData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun getServerUrl(): String? = serverConfigStore.currentServer.value?.serverApiUrl

    fun fetch(type: String, id: String, force: Boolean = false) {
        val serverUrl = getServerUrl() ?: run {
            _error.value = "No server selected"
            return
        }

        if (!force && _infoData.value?.id == id && _infoData.value?.type == type) return

        scope.launch {
            _isLoading.value = true
            _error.value = null

            CoroutineScope(Dispatchers.IO).launch {
                repository.fetch(serverUrl, type, id).collect { result ->
                    result.fold(
                        onSuccess = { items -> _infoData.value = items },
                        onFailure = { _error.value = it.message ?: "Failed to fetch info data" }
                    )
                    _isLoading.value = false
                }
            }
        }
    }

    fun clearData() {
        _infoData.value = null
        _error.value = null
        _isLoading.value = false
    }

    fun clearError() {
        _error.value = null
    }

    fun setInfoParams(type: String, id: String) {
        _infoData.value = null
        _type.value = type
        _id.value = id

        fetch(type, id)
    }
}
