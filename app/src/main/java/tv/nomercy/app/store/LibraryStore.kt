package tv.nomercy.app.store

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tv.nomercy.app.api.models.Component
import tv.nomercy.app.api.models.Library
import tv.nomercy.app.api.models.MediaItem
import tv.nomercy.app.api.repository.LibraryRepository

/**
 * Store for managing library state and data
 * This store specifically handles SERVER-based library data
 */
class LibraryStore(
    private val context: Context,
    private val authStore: AuthStore,
    private val appConfigStore: AppConfigStore
) {
    private val authService = GlobalStores.getAuthService(context)
    private val repository = LibraryRepository(context, authStore, authService)
    private val scope = CoroutineScope(Dispatchers.IO)

    // Library state
    private val _libraries = MutableStateFlow<List<Library>>(emptyList())
    val libraries: StateFlow<List<Library>> = _libraries.asStateFlow()

    private val _isLoadingLibraries = MutableStateFlow(false)
    val isLoadingLibraries: StateFlow<Boolean> = _isLoadingLibraries.asStateFlow()

    private val _librariesError = MutableStateFlow<String?>(null)
    val librariesError: StateFlow<String?> = _librariesError.asStateFlow()

     private val _libraryItems = MutableStateFlow<Map<String, List<Component<MediaItem>>>>(emptyMap())
     val libraryItems: StateFlow<Map<String, List<Component<MediaItem>>>> = _libraryItems.asStateFlow()

    private val _currentLibraryId = MutableStateFlow<String?>(null)
    val currentLibraryId: StateFlow<String?> = _currentLibraryId.asStateFlow()

    val currentLibrary: StateFlow<List<Component<MediaItem>>> =
        combine(_currentLibraryId, _libraryItems) { libraryId, libraryMap ->
            libraryId?.let { libraryMap[it] } ?: emptyList()
        }.stateIn(
            scope,
            SharingStarted.Lazily,
            emptyList()
        )

    /**
     * Get current server URL from app config store
     */
    private fun getCurrentServerUrl(): String? {
        return appConfigStore.currentServer.value?.serverApiUrl
    }

    /**
     * Fetch all libraries from the current server
     */
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

    fun fetchLibrary(link: String, page: Int = 1, limit: Int = 20) {
        val serverUrl = getCurrentServerUrl()
        if (serverUrl == null) {
            _librariesError.value = "No server selected"
            return
        }

        scope.launch {
            _isLoadingLibraries.value = true
            _librariesError.value = null

            repository.getLibraryItems(serverUrl, link, page, limit).collect { result ->
                result.fold(
                    onSuccess = { component ->
                        // Handle the fetched component data as needed
                        println("DEBUG LibraryStore: Fetched library items for $link: ${component.size} items")

                        // Update the map with new data
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

    /**
     * Clear all library data (useful when switching servers)
     */
    fun clearLibraryData() {
        _libraries.value = emptyList()
        _librariesError.value = null
    }

    /**
     * Clear library error message
     */
    fun clearLibraryError() {
        _librariesError.value = null
    }

    fun setCurrentLibraryId(libraryId: String?) {
        _currentLibraryId.value = libraryId
    }

    fun setIsLoading(value: Boolean) {
        _isLoadingLibraries.value = value
    }
}
