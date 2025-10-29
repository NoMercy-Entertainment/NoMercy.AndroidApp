package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.repositories.LibrariesRepository
import tv.nomercy.app.shared.models.Component

class LibrariesStore(
    context: Context,
    authStore: AuthStore,
    private val serverConfigStore: ServerConfigStore,
) {
    private val authService = GlobalStores.getAuthService(context)
    private val repository = LibrariesRepository(context, authStore, authService)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _librariesData = MutableStateFlow<List<Component>>(emptyList())
    val librariesData: StateFlow<List<Component>> = _librariesData.asStateFlow()

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

        if (!force && _librariesData.value.isNotEmpty()) return

        scope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getLibraries(serverUrl).collect { result ->
                result.fold(
                    onSuccess = { items -> _librariesData.value = items },
                    onFailure = {
                        _error.value = it.message ?: "Failed to fetch libraries data"
                    }
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
