package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.Library
import tv.nomercy.app.shared.models.MediaItem
import tv.nomercy.app.shared.api.repository.LibraryRepository
import kotlin.fold

class LibraryStore(
    private val context: Context,
    private val authStore: AuthStore,
    private val appConfigStore: AppConfigStore
) {
    private val authService = GlobalStores.getAuthService(context)
    private val repository = LibraryRepository(context, authStore, authService)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _libraries = MutableStateFlow<List<Library>>(emptyList())
    val libraries = _libraries.asStateFlow()

    private val _isLoadingLibraries = MutableStateFlow(false)
    val isLoadingLibraries = _isLoadingLibraries.asStateFlow()

    private val _librariesError = MutableStateFlow<String?>(null)
    val librariesError = _librariesError.asStateFlow()

    private val _libraryItems = MutableStateFlow<Map<String, List<Component<MediaItem>>>>(emptyMap())
    val libraryItems = _libraryItems.asStateFlow()

    private fun getServerUrl(): String? = appConfigStore.currentServer.value?.serverApiUrl

    fun fetchLibraries() {
        val serverUrl = getServerUrl() ?: run {
            _librariesError.value = "No server selected"
            return
        }

        scope.launch {
            _isLoadingLibraries.value = true
            _librariesError.value = null

            repository.getLibraries(serverUrl).collect { result ->
                result.fold(
                    onSuccess = { _libraries.value = it },
                    onFailure = { _librariesError.value = it.message ?: "Failed to fetch libraries" }
                )
                _isLoadingLibraries.value = false
            }
        }
    }

    fun fetchLibrary(link: String, page: Int = 1, limit: Int = 20, force: Boolean = false) {
        val serverUrl = getServerUrl() ?: run {
            _librariesError.value = "No server selected"
            return
        }

        if (!force && _libraryItems.value.containsKey(link)) return

        scope.launch {
            _isLoadingLibraries.value = true
            _librariesError.value = null

            repository.getLibraryItems(serverUrl, link, page, limit).collect { result ->
                result.fold(
                    onSuccess = { items ->
                        _libraryItems.value = _libraryItems.value.toMutableMap().apply {
                            this[link] = items
                        }
                    },
                    onFailure = { _librariesError.value = it.message ?: "Failed to fetch items for $link" }
                )
                _isLoadingLibraries.value = false
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
}