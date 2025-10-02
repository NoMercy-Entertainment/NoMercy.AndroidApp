package tv.nomercy.app.ui.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.StateFlow
import tv.nomercy.app.api.models.Component
import tv.nomercy.app.api.models.Library
import tv.nomercy.app.api.models.MediaItem
import tv.nomercy.app.store.AppConfigStore
import tv.nomercy.app.store.LibraryStore

/**
 * ViewModel for managing library data in the Libraries screen
 * Now uses LibraryStore and supports dynamic component rendering
 */
class LibrariesViewModel(
    private val libraryStore: LibraryStore,
    private val appConfigStore: AppConfigStore
) : ViewModel() {

    // Expose LibraryStore state directly
    val libraries: StateFlow<List<Library>> = libraryStore.libraries
    val libraryItems: StateFlow<Map<String, List<Component<MediaItem>>>> = libraryStore.libraryItems
    val isLoading: StateFlow<Boolean> = libraryStore.isLoadingLibraries
    val errorMessage: StateFlow<String?> = libraryStore.librariesError
    val currentLibraryId: StateFlow<String?> = libraryStore.currentLibraryId
    val currentLibrary : StateFlow<List<Component<MediaItem>>> = libraryStore.currentLibrary

    fun loadLibraries() {
        println("DEBUG ViewModel: loadLibraries() called, delegating to LibraryStore")
        libraryStore.fetchLibraries()
    }

    fun loadLibrary(link: String?, forceRefresh: Boolean = false) {
        if (!forceRefresh && link == currentLibraryId.value && currentLibrary.value.isNotEmpty()) {
            return
        }
        val library = libraries.value.find { it.link == link }
        if (library != null) {
            println("DEBUG ViewModel: Loading items for library: ${library.id}")
            libraryStore.fetchLibrary(library.link, page = 0, limit = 50)
        } else {
            println("DEBUG ViewModel: No library found for: $link")
        }
    }

    /**
     * Refresh all library data
     */
    fun refresh() {
        loadLibraries()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        libraryStore.clearLibraryError()
    }

    fun setCurrentLibraryId(libraryId: String?) {
        libraryStore.setCurrentLibraryId(libraryId)
    }

    fun setIsLoading(value: Boolean) {
        libraryStore.setIsLoading(value)
    }
}

/**
 * Factory for creating LibrariesViewModel
 */
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
