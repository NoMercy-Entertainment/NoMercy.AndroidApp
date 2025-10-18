package tv.nomercy.app.views.music.cards.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.models.NMMusicHomeCardProps
import tv.nomercy.app.shared.models.NMGridProps
import tv.nomercy.app.shared.stores.CardsStore

class CardsViewModel(
    private val cardStore: CardsStore
) : ViewModel() {

    val isLoading = cardStore.isLoading
    val errorMessage = cardStore.error

    private val _currentType = MutableStateFlow<String?>(null)
    private val _currentChar = MutableStateFlow('#')

    val currentType = _currentType.asStateFlow()
    val currentChar = _currentChar.asStateFlow()

    // Indexer state
    private val _currentCardType = MutableStateFlow<String?>(null)
    private val _hasIndexableContent = MutableStateFlow(false)
    val showIndexer = combine(_currentCardType, _hasIndexableContent) { type, hasContent ->
        type in setOf("albums", "artists") || hasContent
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _activeIndexerLetters = MutableStateFlow<Set<Char>>(emptySet())
    val activeIndexerLetters = _activeIndexerLetters.asStateFlow()

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex = _selectedIndex.asStateFlow()

    // Expose the list of components for the currently selected type/char
    val cards = combine(currentType, currentChar, cardStore.cardItems) { type, char, map ->
        if (type == null) emptyList() else map["music/$type/$char"] ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            combine(currentType, currentChar) { t, c -> t to c }
                .collect { (t, c) ->
                    if (t != null) {
                        _currentCardType.value = t
                        cardStore.fetchCards(t, c)
                    }
                }
        }

        // Compute active letters and whether content is indexable
        viewModelScope.launch {
            combine(cards, isLoading) { components, loading -> components to loading }
                .collect { (components, loading) ->
                    if (!loading) {
                        _hasIndexableContent.value = components.any { component ->
                            val gridProps = component.props as? NMGridProps
                            component.component == "NMGrid" &&
                                    gridProps != null &&
                                    gridProps.items.any { item ->
                                        val cardProps = item.props as? NMMusicHomeCardProps
                                        item.component == "NMMusicCard" &&
                                                cardProps != null &&
                                                (cardProps.data?.name?.isNotBlank() == true)
                                    }
                        }
                    }

                    val containsAlbumOrArtist = components.any { component ->
                        val gridProps = component.props as? NMGridProps
                        component.component == "NMGrid" &&
                                gridProps != null &&
                                gridProps.items.any { item ->
                                    val cardProps = item.props as? NMMusicHomeCardProps
                                    item.component == "NMMusicCard" &&
                                            cardProps != null &&
                                            (cardProps.data?.type == "albums" || cardProps.data?.type == "artists")
                                }
                    }

                    val activeLetters = if (containsAlbumOrArtist || _currentCardType.value in setOf("albums", "artists")) {
                        (listOf('#') + ('A'..'Z')).toSet()
                    } else {
                        components
                            .filter { it.component == "NMGrid" }
                            .flatMap { (it.props as? NMGridProps)?.items ?: emptyList() }
                            .filter { it.component == "NMMusicCard" }
                            .mapNotNull { (it.props as? NMMusicHomeCardProps)?.data?.name }
                            .mapNotNull { name ->
                                val trimmed = name.trim()
                                val firstChar = trimmed.firstOrNull { it.isLetterOrDigit() }
                                firstChar?.uppercaseChar()?.let { if (!it.isLetter()) '#' else it }
                            }
                            .toSet()
                    }

                    _activeIndexerLetters.value = activeLetters
                }
        }
    }

    fun selectCard(type: String, char: Char? = null) {
        val selectedChar = char ?: '#'
        if (_currentType.value == type && _currentChar.value == selectedChar) return
        _currentType.value = type
        _currentChar.value = selectedChar
        _selectedIndex.value = (listOf('#') + ('A'..'Z')).indexOf(selectedChar).coerceAtLeast(0)
    }

    fun onIndexSelected(char: Char) {
        val index = (listOf('#') + ('A'..'Z')).indexOf(char)
        _selectedIndex.value = index
        val t = _currentType.value ?: return
        _currentChar.value = char
        cardStore.fetchCards(t, char, force = true)
    }

    fun refresh() {
        val t = _currentType.value ?: return
        val c = _currentChar.value
        cardStore.fetchCards(t, c, force = true)
    }

    fun clearError() = cardStore.clearError()

}

class CardsViewModelFactory(
    private val cardStore: CardsStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardsViewModel::class.java)) {
            return CardsViewModel(
                cardStore = cardStore
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}