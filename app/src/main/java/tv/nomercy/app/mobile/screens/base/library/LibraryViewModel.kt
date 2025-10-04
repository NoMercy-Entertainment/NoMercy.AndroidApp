package tv.nomercy.app.mobile.screens.base.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.Library
import tv.nomercy.app.shared.models.MediaItem
import tv.nomercy.app.shared.stores.AppConfigStore
import tv.nomercy.app.shared.stores.LibraryStore

class LibrariesViewModel(
    private val libraryStore: LibraryStore,
    private val appConfigStore: AppConfigStore
) : ViewModel() {

    val libraries = libraryStore.libraries
    val isLoading = libraryStore.isLoadingLibraries
    val errorMessage = libraryStore.librariesError

    private val _currentLibraryId = MutableStateFlow<String?>(null)
    val currentLibraryId = _currentLibraryId.asStateFlow()

    private val _libraryLetterSelections = MutableStateFlow<Map<String, Char>>(emptyMap())

    val currentLibrary = combine(
        currentLibraryId,
        libraryStore.libraryItems,
        _libraryLetterSelections,
        libraries
    ) { id, itemsMap, letterSelections, libs ->
        val path = getLibraryPath(id, libs, letterSelections)
        path?.let { itemsMap[it] } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _showIndexer = MutableStateFlow(false)
    val showIndexer = _showIndexer.asStateFlow()

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex = _selectedIndex.asStateFlow()

    private val _scrollRequest = MutableStateFlow<Int?>(null)
    val scrollRequest = _scrollRequest.asStateFlow()

    val indexerCharacters = listOf('#') + ('A'..'Z').toList()

    private val _isEmptyStable = MutableStateFlow(false)
    val isEmptyStable = _isEmptyStable.asStateFlow()

    private var emptyCheckJob: Job? = null

    init {
        viewModelScope.launch {
            currentLibrary.collect { library ->
                _showIndexer.value = library.any { it.component == "NMGrid" }
            }
        }

        viewModelScope.launch {
            combine(currentLibraryId, _libraryLetterSelections) { _, _ -> }
                .collect { loadCurrentLibrary() }
        }
    }

    private fun getLibraryPath(
        id: String?,
        libs: List<Library>,
        selections: Map<String, Char>
    ): String? {
        val library = libs.find { it.link == id } ?: return id
        return if (library.type == "movie") {
            selections[library.id]?.let { "libraries/${library.id}/letter/$it" } ?: library.link
        } else {
            library.link
        }
    }

    private fun loadCurrentLibrary(forceRefresh: Boolean = false) {
        val id = currentLibraryId.value ?: return
        val path = getLibraryPath(id, libraries.value, _libraryLetterSelections.value)
        libraryStore.fetchLibrary(path ?: id, page = 0, limit = 50, force = forceRefresh)
    }

    fun onIndexSelected(char: Char) {
        val index = indexerCharacters.indexOf(char)
        _selectedIndex.value = index

        val id = currentLibraryId.value ?: return
        val library = libraries.value.find { it.link == id }

        if (library?.type == "movie") {
            _libraryLetterSelections.update { it + (library.id to char) }
        } else {
            viewModelScope.launch {
                val gridItems = currentLibrary.value.firstOrNull { it.component == "NMGrid" }?.props?.items
                val itemIndex = gridItems?.indexOfFirst {
                    it.props.data?.title?.startsWith(char, ignoreCase = true) == true
                } ?: -1
                if (itemIndex != -1) _scrollRequest.value = itemIndex
            }
        }
    }

    fun onScrollRequestCompleted() {
        _scrollRequest.value = null
    }

    fun selectLibrary(libraryId: String?) {
        if (_currentLibraryId.value == libraryId) return
        _currentLibraryId.value = libraryId

        val library = libraries.value.find { it.link == libraryId }
        if (library?.type == "movie" && !_libraryLetterSelections.value.containsKey(library.id)) {
            _libraryLetterSelections.update { it + (library.id to 'A') }
            _selectedIndex.value = indexerCharacters.indexOf('A')
        }

        _scrollRequest.value = 0
    }

    fun refresh() = loadCurrentLibrary(forceRefresh = true)

    fun clearError() = libraryStore.clearLibraryError()

    fun loadLibraries() = libraryStore.fetchLibraries()

    fun checkIfEmpty(content: List<Component<MediaItem>>) {
        emptyCheckJob?.cancel()
        emptyCheckJob = viewModelScope.launch {
            delay(150)
            _isEmptyStable.value = content.isEmpty()
        }
    }

    fun setSelectedIndexFromScroll(index: Int) {
        _selectedIndex.value = index
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
