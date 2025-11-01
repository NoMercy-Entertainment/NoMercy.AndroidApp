package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.repositories.MusicStartRepository
import tv.nomercy.app.shared.models.Component

class MusicStartStore(
    context: Context,
    authStore: AuthStore,
    private val serverConfigStore: ServerConfigStore,
) {
    private val authService = GlobalStores.getAuthService(context)
    private val repository = MusicStartRepository(context, authStore, authService)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _musicStartData = MutableStateFlow<List<Component>>(emptyList())
    val musicStartData: StateFlow<List<Component>> = _musicStartData.asStateFlow()

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

        if (!force && _musicStartData.value.isNotEmpty()) return

        scope.launch {
            _isLoading.value = true
            _error.value = null

            repository.fetch(serverUrl).collect { result ->
                result.fold(
                    onSuccess = { items -> _musicStartData.value = items },
                    onFailure = {
                        _error.value = it.message ?: "Failed to fetch musicStart data"
                    }
                )
                _isLoading.value = false
            }
        }
    }


    fun clearData() {
        _musicStartData.value = emptyList()
        _error.value = null
        _isLoading.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
