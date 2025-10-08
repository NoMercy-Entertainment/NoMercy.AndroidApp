package tv.nomercy.app.mobile.screens.base.libraries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.stores.LibrariesStore


class LibrariesViewModel(
    private val librariesStore: LibrariesStore,
) : ViewModel() {

    val librariesData= librariesStore.librariesData

    val isLoading = librariesStore.isLoading
    val errorMessage = librariesStore.error

    private val _isEmptyStable = MutableStateFlow(false)
    val isEmptyStable = _isEmptyStable.asStateFlow()

    init {
        viewModelScope.launch {
            loadLibrariesData()
        }
    }

    fun loadLibrariesData(forceRefresh: Boolean = false) {
        librariesStore.fetch(force = forceRefresh)
    }

    fun refresh() {
        loadLibrariesData(forceRefresh = true)
    }

    fun clearError() {
        librariesStore.clearError()
    }
}

class LibrariesViewModelFactory(
    private val librariesStore: LibrariesStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibrariesViewModel::class.java)) {
            return LibrariesViewModel(
                librariesStore = librariesStore
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
