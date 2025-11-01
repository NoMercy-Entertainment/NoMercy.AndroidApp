package tv.nomercy.app.views.base.search.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.stores.SearchStore

enum class SearchBarPosition { Closed, Open, Contents }
enum class SearchType { Video, Music }

class SearchViewModel(
    private val searchStore: SearchStore
) : ViewModel() {

    var searchType = searchStore.searchType

    var searchQuery = searchStore.searchQuery

    val isLoading = searchStore.isLoading
    val error = searchStore.error

    val musicResults = searchStore.musicResults
    val videoResults = searchStore.videoResults

    private val debouncer = Debouncer(viewModelScope, 700L)

    fun onSearchQueryChanged(query: String) {
        searchStore.setSearchQuery(query)
        debouncer.submit {
            when (searchType.value) {
                SearchType.Music -> performMusicSearch(query)
                SearchType.Video -> performVideoSearch(query)
            }
        }
    }

    fun onSearchTypeChanged(type: SearchType) {
        searchStore.setSearchType(type)
    }

    private fun performMusicSearch(query: String) {
        if (query.isBlank()) {
            searchStore.clearMusic()
            return
        }

        viewModelScope.launch {
            searchStore.searchMusic(query)
        }
    }

    private fun performVideoSearch(query: String) {
        if (query.isBlank()) {
            searchStore.clearVideo()
            return
        }

        viewModelScope.launch {
            searchStore.searchVideo(query)
        }
    }
}

class Debouncer(
    private val scope: CoroutineScope,
    private val delayMillis: Long
) {
    private var debounceJob: Job? = null

    fun submit(action: suspend () -> Unit) {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(delayMillis)
            action()
        }
    }
}

class SearchViewModelFactory(
    private val searchStore: SearchStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(
                searchStore = searchStore,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
