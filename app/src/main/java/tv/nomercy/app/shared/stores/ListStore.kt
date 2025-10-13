package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.repositories.ListRepository
import tv.nomercy.app.shared.models.MusicList

class ListStore(
    context: Context,
    authStore: AuthStore,
    private val serverConfigStore: ServerConfigStore
) {
    private val authService = GlobalStores.getAuthService(context)
    private val repository = ListRepository(context, authStore, authService)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _listItems = MutableStateFlow<Map<String, MusicList>>(emptyMap())
    val listItems: StateFlow<Map<String, MusicList>> = _listItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun getServerUrl(): String? = serverConfigStore.currentServer.value?.serverApiUrl

    fun fetchListItems(type: String, id: String, force: Boolean = false) {
        val serverUrl = getServerUrl() ?: run {
            _error.value = "No server selected"
            return
        }

        val key = "music/$type/$id"

        if (!force && _listItems.value.containsKey(key)) return

        scope.launch {
            _isLoading.value = true
            _error.value = null

            repository.fetchList(serverUrl, type, id).collect { result ->
                result.fold(
                    onSuccess = { items ->
                        _listItems.update { it + (key to items) }
                    },
                    onFailure = { _error.value = it.message ?: "Failed to fetch items for $type" }
                )
                _isLoading.value = false
            }
        }
    }

    fun clearData() {
        _listItems.value = emptyMap()
        _error.value = null
        _isLoading.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
