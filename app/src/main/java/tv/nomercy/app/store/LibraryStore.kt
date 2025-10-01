package tv.nomercy.app.store

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.nomercy.app.api.models.Library
import tv.nomercy.app.api.models.LibraryStats
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

    // Media items state
    private val _mediaItems = MutableStateFlow<Map<String, List<MediaItem>>>(emptyMap())
    val mediaItems: StateFlow<Map<String, List<MediaItem>>> = _mediaItems.asStateFlow()

    private val _isLoadingMediaItems = MutableStateFlow<Set<String>>(emptySet())
    val isLoadingMediaItems: StateFlow<Set<String>> = _isLoadingMediaItems.asStateFlow()

    // Library stats state
    private val _libraryStats = MutableStateFlow<Map<String, LibraryStats>>(emptyMap())
    val libraryStats: StateFlow<Map<String, LibraryStats>> = _libraryStats.asStateFlow()

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

    /**
     * Fetch media items for a specific library from the current server
     */
    fun fetchLibraryItems(libraryId: String, page: Int = 1, limit: Int = 20) {
        val serverUrl = getCurrentServerUrl()
        if (serverUrl == null) {
            return
        }

        scope.launch {
            _isLoadingMediaItems.value = _isLoadingMediaItems.value + libraryId

            repository.getLibraryItems(serverUrl, libraryId, page, limit).collect { result ->
                result.fold(
                    onSuccess = { items ->
                        val currentItems = _mediaItems.value[libraryId] ?: emptyList()
                        val updatedItems = if (page == 1) items else currentItems + items
                        _mediaItems.value = _mediaItems.value + (libraryId to updatedItems)
                        _isLoadingMediaItems.value = _isLoadingMediaItems.value - libraryId
                    },
                    onFailure = { error ->
                        _isLoadingMediaItems.value = _isLoadingMediaItems.value - libraryId
                        // Could add error handling for specific library items
                    }
                )
            }
        }
    }

    /**
     * Fetch statistics for a specific library from the current server
     */
    fun fetchLibraryStats(libraryId: String) {
        val serverUrl = getCurrentServerUrl()
        if (serverUrl == null) {
            return
        }

        scope.launch {
            repository.getLibraryStats(serverUrl, libraryId).collect { result ->
                result.fold(
                    onSuccess = { stats ->
                        _libraryStats.value = _libraryStats.value + (libraryId to stats)
                    },
                    onFailure = { error ->
                        // Could add error handling for library stats
                    }
                )
            }
        }
    }

    /**
     * Get libraries by type (movie, tv, music, collection)
     */
    fun getLibrariesByType(type: String): List<Library> {
        return _libraries.value.filter { it.type.equals(type, ignoreCase = true) }
    }

    /**
     * Get media items for a specific library
     */
    fun getMediaItemsForLibrary(libraryId: String): List<MediaItem> {
        return _mediaItems.value[libraryId] ?: emptyList()
    }

    /**
     * Get stats for a specific library
     */
    fun getStatsForLibrary(libraryId: String): LibraryStats? {
        return _libraryStats.value[libraryId]
    }

    /**
     * Clear all library data (useful when switching servers)
     */
    fun clearLibraryData() {
        _libraries.value = emptyList()
        _mediaItems.value = emptyMap()
        _libraryStats.value = emptyMap()
        _librariesError.value = null
    }

    /**
     * Refresh libraries when server changes
     */
    fun onServerChanged() {
        clearLibraryData()
        fetchLibraries()
    }
}
