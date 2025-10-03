package tv.nomercy.app.ui.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tv.nomercy.app.api.models.Component
import tv.nomercy.app.api.models.Library
import tv.nomercy.app.api.models.MediaItem
import tv.nomercy.app.store.AppConfigStore
import tv.nomercy.app.store.LibraryStore

class LibrariesViewModel(
    private val libraryStore: LibraryStore,
    private val appConfigStore: AppConfigStore
) : ViewModel() {

    val libraries: StateFlow<List<Library>> = libraryStore.libraries
    val isLoading: StateFlow<Boolean> = libraryStore.isLoadingLibraries
    val errorMessage: StateFlow<String?> = libraryStore.librariesError

    private val _currentLibraryId = MutableStateFlow<String?>(null)
    val currentLibraryId: StateFlow<String?> = _currentLibraryId.asStateFlow()

    private val _libraryLetterSelections = MutableStateFlow<Map<String, Char>>(emptyMap())

    val currentLibrary: StateFlow<List<Component<MediaItem>>> = combine(
        currentLibraryId,
        libraryStore.libraryItems,
        _libraryLetterSelections
    ) { id, itemsMap, letterSelections ->
        val library = libraries.value.find { it.link == id }
        val path = if (library?.type == "movie") {
            val letter = letterSelections[library.id]
            if (letter != null) "libraries/${library.id}/letter/$letter" else library.link
        } else {
            id
        }
        path?.let { itemsMap[it] } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _showIndexer = MutableStateFlow(false)
    val showIndexer: StateFlow<Boolean> = _showIndexer.asStateFlow()

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex: StateFlow<Int> = _selectedIndex.asStateFlow()

    private val _scrollRequest = MutableStateFlow<Int?>(null)
    val scrollRequest: StateFlow<Int?> = _scrollRequest.asStateFlow()

    val indexerCharacters = listOf('#') + ('A'..'Z').toList()

    init {
        viewModelScope.launch {
            currentLibrary.collect { library ->
                _showIndexer.value = library.any { it.component == "NMGrid" }
            }
        }
    }

    fun onIndexSelected(char: Char) {
        val index = indexerCharacters.indexOf(char)
        _selectedIndex.value = index
        val libraryId = currentLibraryId.value ?: return
        val library = libraries.value.find { it.link == libraryId }

        if (library?.type == "movie") {
            val currentSelections = _libraryLetterSelections.value.toMutableMap()
            currentSelections[library.id] = char
            _libraryLetterSelections.value = currentSelections

            val linkToLoad = if (char == 'A') {
                library.link
            } else {
                "libraries/${library.id}/letter/$char"
            }
            loadLibrary(linkToLoad, forceRefresh = true)
        } else {
            viewModelScope.launch {
                val gridItems = this@LibrariesViewModel.currentLibrary.value.firstOrNull { it.component == "NMGrid" }?.props?.items
                if (gridItems != null) {
                    val itemIndex = gridItems.indexOfFirst { 
                        it.props.data?.title?.startsWith(char, ignoreCase = true) == true 
                    }
                    if (itemIndex != -1) {
                        _scrollRequest.value = itemIndex
                    }
                }
            }
        }
    }

    fun onScrollRequestCompleted() {
        _scrollRequest.value = null
    }

    fun loadLibraries() {
        libraryStore.fetchLibraries()
    }

    private fun loadLibrary(link: String?, forceRefresh: Boolean = false) {
        if (link == null) return

        libraryStore.fetchLibrary(link, page = 0, limit = 50, force = forceRefresh)
    }

    fun refresh() {
        loadLibrary(currentLibraryId.value, forceRefresh = true)
    }

    fun clearError() {
        libraryStore.clearLibraryError()
    }

    fun selectLibrary(libraryId: String?) {
        if (currentLibraryId.value == libraryId) return
        _currentLibraryId.value = libraryId
        if (libraryId != null) {
            val library = libraries.value.find { it.link == libraryId }
            if (library?.type == "movie") {
                if (!_libraryLetterSelections.value.containsKey(library.id)) {
                    val currentSelections = _libraryLetterSelections.value.toMutableMap()
                    val defaultChar = 'A'
                    currentSelections[library.id] = defaultChar
                    _libraryLetterSelections.value = currentSelections
                    val index = indexerCharacters.indexOf(defaultChar)
                    _selectedIndex.value = index
                }
            }
            loadLibrary(libraryId)
        }
    }
}

class LibrariesViewModelFactory(
    private val libraryStore: LibraryStore,
    private val appConfigStore: AppConfigStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibrariesViewModel::class.java)) {
            return LibrariesViewModel(libraryStore, appConfigStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
