package tv.nomercy.app.mobile.screens.base.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.models.Library
import tv.nomercy.app.shared.stores.LibraryStore

class LibrariesViewModel(
    private val libraryStore: LibraryStore
) : ViewModel() {

    val libraries = libraryStore.libraries
    val isLoading = libraryStore.isLoading
    val errorMessage = libraryStore.error

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

    private val _currentLibraryType = MutableStateFlow<String?>(null)

    private val _hasIndexableContent = MutableStateFlow(false)

    // Derive showIndexer from library type OR indexable content - stable during loading
    val showIndexer = combine(_currentLibraryType, _hasIndexableContent) { type, hasContent ->
        type == "movie" || hasContent
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _activeIndexerLetters = MutableStateFlow<Set<Char>>(emptySet())
    val activeIndexerLetters = _activeIndexerLetters.asStateFlow()

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex = _selectedIndex.asStateFlow()

    private val _scrollRequest = MutableStateFlow<Int?>(null)
    val scrollRequest = _scrollRequest.asStateFlow()

    val indexerCharacters = listOf('#') + ('A'..'Z').toList()

    private val _isEmptyStable = MutableStateFlow(false)
    val isEmptyStable = _isEmptyStable.asStateFlow()

    init {
        viewModelScope.launch {
            combine(currentLibrary, isLoading) { components, loading ->
                components to loading
            }.collect { (components, loading) ->

                // Update isEmptyStable only when not loading
                if (!loading) {
                    _isEmptyStable.value = components.isEmpty()

                    // Check if content has indexable items (for non-paginated libraries)
                    val hasIndexableContent = components.any {
                        it.component == "NMGrid" &&
                                it.props.items.any { item ->
                                    item.component == "NMCard" &&
                                            item.props.data?.type in setOf("movie", "tv")
                                }
                    }
                    _hasIndexableContent.value = hasIndexableContent
                }

                val containsMovie = components.any {
                    it.component == "NMGrid" &&
                            it.props.items.any { item ->
                                item.component == "NMCard" && item.props.data?.type == "movie"
                            }
                }

                val activeLetters = if (containsMovie || _currentLibraryType.value == "movie") {
                    indexerCharacters.toSet()
                } else {
                    components
                        .filter { it.component == "NMGrid" }
                        .flatMap { it.props.items }
                        .filter { it.component == "NMCard" }
                        .mapNotNull { it.props.data?.titleSort }
                        .mapNotNull { sortTitle ->
                            val trimmed = sortTitle.trim()
                            val firstChar = trimmed.firstOrNull { it.isLetterOrDigit() }
                            firstChar?.uppercaseChar()?.let { if (!it.isLetter()) '#' else it }
                        }
                        .toSet()
                }

                _activeIndexerLetters.value = activeLetters
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
        if (id == null) return null

        // Extract base library ID (before /letter/ if present)
        val baseLibraryId = id.substringBefore("/letter/")

        // Extract just the UUID part
        val libraryIdOnly = baseLibraryId.substringAfterLast("/")

        // Find library by UUID (id field)
        val library = libs.find { it.id == libraryIdOnly } ?: return id

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

        // If route contains /letter/, use letter pagination, otherwise scroll
        if (id.contains("/letter/")) {
            val baseLibraryId = id.substringBefore("/letter/")
            val libraryIdOnly = baseLibraryId.substringAfterLast("/")
            val library = libraries.value.find { it.id == libraryIdOnly }

            if (library != null) {
                _libraryLetterSelections.update { it + (library.id to char) }
            }
        } else {
            // Scroll-based navigation for non-paginated libraries
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

    fun selectLibrary(libraryId: String) {
        if (_currentLibraryId.value == libraryId) return

        val baseLibraryId = libraryId.substringBefore("/letter/")
        val letterSegment = libraryId.substringAfter("/letter/", "")
        val letterFromRoute = letterSegment.firstOrNull()?.uppercaseChar()
            ?.takeIf { it in indexerCharacters }

        val library = libraries.value.find { it.link == baseLibraryId }
            ?: libraries.value.find { it.id == baseLibraryId }

        // Store library type - this drives indexer visibility
        _currentLibraryType.value = library?.type

        if (library?.type == "movie") {
            val selectedLetter = letterFromRoute
                ?: _libraryLetterSelections.value[library.id]
                ?: 'A'

            _libraryLetterSelections.update { it + (library.id to selectedLetter) }
            _selectedIndex.value = indexerCharacters.indexOf(selectedLetter)
            _currentLibraryId.value = "libraries/${library.id}/letter/$selectedLetter"
        } else {
            _currentLibraryId.value = libraryId
        }

        _scrollRequest.value = 0
    }

    fun refresh() = loadCurrentLibrary(forceRefresh = true)

    fun clearError() = libraryStore.clearError()

    fun setSelectedIndexFromScroll(index: Int) {
        if(isLoading.value) return
        _selectedIndex.value = index
    }
}

class LibrariesViewModelFactory(
    private val libraryStore: LibraryStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibrariesViewModel::class.java)) {
            return LibrariesViewModel(
                libraryStore = libraryStore
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}