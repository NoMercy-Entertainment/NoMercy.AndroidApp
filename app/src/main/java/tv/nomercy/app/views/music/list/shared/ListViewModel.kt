package tv.nomercy.app.views.music.list.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tv.nomercy.app.shared.stores.ListStore


class ListViewModel(
    private val listStore: ListStore
) : ViewModel() {

    val isLoading = listStore.isLoading
    val errorMessage = listStore.error

    private val _currentType = MutableStateFlow<String?>(null)
    private val _currentId = MutableStateFlow<String?>(null)

    val currentType = _currentType.asStateFlow()

    val currentId = _currentId.asStateFlow()

    val list = combine(currentType, currentId, listStore.listItems) { type, id, map ->
        if (type == null || id == null) null else map["music/$type/$id"]
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun selectList(type: String, id: String) {
        _currentType.value = type
        _currentId.value = id
        listStore.fetchListItems(type, id)
    }

    fun refresh() {
        val type = _currentType.value ?: return
        val id = _currentId.value ?: return
        listStore.fetchListItems(type, id, force = true)
    }

    fun clearError() {
        listStore.clearError()
    }
}

class ListViewModelFactory(
    private val listStore: ListStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            return ListViewModel(
                listStore = listStore
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}