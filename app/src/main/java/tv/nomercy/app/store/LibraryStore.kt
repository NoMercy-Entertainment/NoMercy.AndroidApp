package tv.nomercy.app.store

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.nomercy.app.api.models.Component
import tv.nomercy.app.api.models.Library
import tv.nomercy.app.api.models.MediaItem
import tv.nomercy.app.api.repository.LibraryRepository

class LibraryStore(
    private val context: Context,
    private val authStore: AuthStore,
    private val appConfigStore: AppConfigStore
) {
    private val authService = GlobalStores.getAuthService(context)
    private val repository = LibraryRepository(context, authStore, authService)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _libraries = MutableStateFlow<List<Library>>(emptyList())
    val libraries: StateFlow<List<Library>> = _libraries.asStateFlow()

    private val _isLoadingLibraries = MutableStateFlow(false)
    val isLoadingLibraries: StateFlow<Boolean> = _isLoadingLibraries.asStateFlow()

    private val _librariesError = MutableStateFlow<String?>(null)
    val librariesError: StateFlow<String?> = _librariesError.asStateFlow()

    private val _libraryItems = MutableStateFlow<Map<String, List<Component<MediaItem>>>>(emptyMap())
    val libraryItems: StateFlow<Map<String, List<Component<MediaItem>>>> = _libraryItems.asStateFlow()

    private fun getCurrentServerUrl(): String? {
        return appConfigStore.currentServer.value?.serverApiUrl
    }

    fun fetchLibraries() {
        val serverUrl = getCurrentServerUrl()
        if (serverUrl == null) {
            _librariesError.value = "No server selected"
            return
        }

        scope.launch {
            _isLoadingLibraries.value = true
            _librariesError.value = null

            repository.getLibraries(serverUrl).collect { result ->
                result.fold(
                    onSuccess = { libraries ->
                        _libraries.value = libraries
                        _isLoadingLibraries.value = false
                    },
                    onFailure = { error ->
                        _librariesError.value = error.message ?: "Failed to fetch libraries"
                        _isLoadingLibraries.value = false
                    }
                )
            }
        }
    }

    fun fetchLibrary(link: String, page: Int = 1, limit: Int = 20, force: Boolean = false) {
        val serverUrl = getCurrentServerUrl()
        if (serverUrl == null) {
            _librariesError.value = "No server selected"
            return
        }

        if (!force && _libraryItems.value.containsKey(link)) {
            return
        }

        scope.launch {
            _isLoadingLibraries.value = true
            _librariesError.value = null

            repository.getLibraryItems(serverUrl, link, page, limit).collect { result ->
                result.fold(
                    onSuccess = { component ->
                        val currentMap = _libraryItems.value.toMutableMap()
                        currentMap[link] = component
                        _libraryItems.value = currentMap
                        _isLoadingLibraries.value = false
                    },
                    onFailure = { error ->
                        _librariesError.value = error.message ?: "Failed to fetch library items for $link"
                        _isLoadingLibraries.value = false
                    }
                )
            }
        }
    }

    fun clearLibraryData() {
        _libraries.value = emptyList()
        _libraryItems.value = emptyMap()
        _librariesError.value = null
        _isLoadingLibraries.value = false
    }

    fun clearLibraryError() {
        _librariesError.value = null
    }

    fun setIsLoading(value: Boolean) {
        _isLoadingLibraries.value = value
    }
}
