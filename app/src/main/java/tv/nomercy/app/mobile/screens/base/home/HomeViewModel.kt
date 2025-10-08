package tv.nomercy.app.mobile.screens.base.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.stores.AuthStore
import tv.nomercy.app.shared.stores.HomeStore


class HomeViewModel(
    private val homeStore: HomeStore,
    private val authStore: AuthStore,
) : ViewModel() {

    val homeData= homeStore.homeData

    val isLoading = homeStore.isLoading
    val errorMessage = homeStore.error

    private val _isEmptyStable = MutableStateFlow(false)
    val isEmptyStable = _isEmptyStable.asStateFlow()

    init {
        viewModelScope.launch {
            loadHomeData()
        }
    }

    fun loadHomeData(forceRefresh: Boolean = false) {
        homeStore.fetch(force = forceRefresh)
    }

    fun refresh() {
        loadHomeData(forceRefresh = true)
    }

    fun clearError() {
        homeStore.clearError()
    }
}

class HomeViewModelFactory(
    private val homeStore: HomeStore,
    private val authStore: AuthStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                homeStore = homeStore,
                authStore = authStore,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
