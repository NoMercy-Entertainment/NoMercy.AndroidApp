package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.repositories.LibraryRepository
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.Library

class LibraryStore(
    context: Context,
    authStore: AuthStore,
    private val serverConfigStore: ServerConfigStore
) {
    private val authService = GlobalStores.getAuthService(context)
    private val repository = LibraryRepository(context, authStore, authService)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _libraries = MutableStateFlow<List<Library>>(emptyList())
    val libraries: StateFlow<List<Library>> = _libraries.asStateFlow()

    private val _libraryItems = MutableStateFlow<Map<String, List<Component>>>(emptyMap())
    val libraryItems: StateFlow<Map<String, List<Component>>> = _libraryItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun getServerUrl(): String? = serverConfigStore.currentServer.value?.serverApiUrl

    fun fetchLibraries() {
        val serverUrl = getServerUrl() ?: run {
            _error.value = "No server selected"
            return
        }

        scope.launch {
            _isLoading.value = true
            _error.value = null

            repository.fetch(serverUrl).collect { result ->
                result.fold(
                    onSuccess = { _libraries.value = it },
                    onFailure = { _error.value = it.message ?: "Failed to fetch libraries" }
                )
                _isLoading.value = false

            }
        }
    }

    fun fetchLibrary(link: String, page: Int = 0, limit: Int = 20, force: Boolean = false) {
        val serverUrl = getServerUrl() ?: run {
            _error.value = "No server selected"
            return
        }

        if (!force && _libraryItems.value.containsKey(link)) return

        scope.launch {
            _isLoading.value = true
            _error.value = null

            CoroutineScope(Dispatchers.IO).launch {
                repository.getLibraryItems(serverUrl, link, page, limit).collect { result ->
                    result.fold(
                        onSuccess = { items ->
                            _libraryItems.update { it + (link to items) }
                        },
                        onFailure = {
                            _error.value = it.message ?: "Failed to fetch items for $link"
                        }
                    )
                    _isLoading.value = false
                }
            }
        }
    }

    fun clearData() {
        _libraries.value = emptyList()
        _libraryItems.value = emptyMap()
        _error.value = null
        _isLoading.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
