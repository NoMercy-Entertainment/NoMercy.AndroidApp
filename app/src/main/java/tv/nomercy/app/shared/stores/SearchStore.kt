package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.repositories.SearchRepository
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.SearchResultElement
import tv.nomercy.app.views.base.search.shared.SearchType

class SearchStore(
    context: Context,
    authStore: AuthStore,
    private val serverConfigStore: ServerConfigStore,
) {
    private val authService = GlobalStores.getAuthService(context)
    val searchRepository = SearchRepository(context, authStore, authService)

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchType = MutableStateFlow(SearchType.Video)
    val searchType: StateFlow<SearchType> = _searchType.asStateFlow()

    private val _musicResults = MutableStateFlow<List<Component>>(emptyList())
    val musicResults: StateFlow<List<Component>> = _musicResults.asStateFlow()

    private val _videoResults = MutableStateFlow<List<SearchResultElement>>(emptyList())
    val videoResults: StateFlow<List<SearchResultElement>> = _videoResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun getServerUrl(): String? = serverConfigStore.currentServer.value?.serverApiUrl

    fun searchMusic(query: String) {
        val serverUrl = getServerUrl() ?: run {
            _error.value = "No server selected"
            return
        }

        if (query.isBlank()) {
            _musicResults.value = emptyList()
            return
        }

        scope.launch {
            _isLoading.value = true
            _error.value = null

            searchRepository.searchMusic(serverUrl, query).collect { result ->
                result.fold(
                    onSuccess = { items -> _musicResults.value = items },
                    onFailure = { _error.value = it.message ?: "Music search failed" }
                )
                _isLoading.value = false
            }
        }
    }

    fun searchVideo(query: String) {
        if (query.isBlank()) {
            _videoResults.value = emptyList()
            return
        }

        scope.launch {
            _isLoading.value = true
            _error.value = null

            searchRepository.searchVideo(query).collect { result ->
                result.fold(
                    onSuccess = { items -> _videoResults.value = items },
                    onFailure = { _error.value = it.message ?: "Video search failed" }
                )
                _isLoading.value = false
            }
        }
    }

    fun clearMusic() {
        _musicResults.value = emptyList()
    }
    fun clearVideo() {
        _videoResults.value = emptyList()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchType(type: SearchType) {
        _searchType.value = type
    }

    fun clearAll() {
        _musicResults.value = emptyList()
        _videoResults.value = emptyList()
        _error.value = null
        _isLoading.value = false
    }
}