package tv.nomercy.app.mobile.screens.music.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.stores.AuthStore
import tv.nomercy.app.shared.stores.MusicStartStore


class MusicStartViewModel(
    private val musicStartStore: MusicStartStore,
    private val authStore: AuthStore,
) : ViewModel() {

    val musicStartData= musicStartStore.musicStartData

    val isLoading = musicStartStore.isLoading
    val errorMessage = musicStartStore.error

    private val _isEmptyStable = MutableStateFlow(false)
    val isEmptyStable = _isEmptyStable.asStateFlow()

    init {
        viewModelScope.launch {
            loadMusicStartData()
        }
    }

    fun loadMusicStartData(forceRefresh: Boolean = false) {
        musicStartStore.fetch(force = forceRefresh)
    }

    fun refresh() {
        loadMusicStartData(forceRefresh = true)
    }

    fun clearError() {
        musicStartStore.clearError()
    }
}

class MusicStartViewModelFactory(
    private val musicStartStore: MusicStartStore,
    private val authStore: AuthStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicStartViewModel::class.java)) {
            return MusicStartViewModel(
                musicStartStore = musicStartStore,
                authStore = authStore,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
