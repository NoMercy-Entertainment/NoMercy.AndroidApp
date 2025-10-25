package tv.nomercy.app.views.base.info.shared


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.nomercy.app.shared.stores.AuthStore
import tv.nomercy.app.shared.stores.InfoStore

class InfoViewModel(
    private val infoStore: InfoStore,
    private val authStore: AuthStore,
) : ViewModel() {

    val infoData = infoStore.infoData

    val isLoading = infoStore.isLoading
    val errorMessage = infoStore.error

    val type = infoStore.type

    val id = infoStore.id

    private val _isEmptyStable = MutableStateFlow(false)
    val isEmptyStable = _isEmptyStable.asStateFlow()

    fun setInfoParams(type: String, id: String) {
        infoStore.setInfoParams(type, id)
    }

    fun refresh() {
        type.value?.let { currentType ->
            id.value?.let { currentId ->
                infoStore.fetch(type = currentType, id = currentId, force = true)
            }
        }
    }

    fun clearError() {
        infoStore.clearError()
    }
}

class InfoViewModelFactory(
    private val infoStore: InfoStore,
    private val authStore: AuthStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InfoViewModel::class.java)) {
            return InfoViewModel(
                infoStore = infoStore,
                authStore = authStore,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
