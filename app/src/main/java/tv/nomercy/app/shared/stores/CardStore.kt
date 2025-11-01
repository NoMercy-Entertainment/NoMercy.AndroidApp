package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.repositories.CardsRepository
import tv.nomercy.app.shared.models.Component

class CardsStore(
    context: Context,
    authStore: AuthStore,
    private val serverConfigStore: ServerConfigStore
) {
    private val authService = GlobalStores.getAuthService(context)
    private val repository = CardsRepository(context, authStore, authService)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _cardItems = MutableStateFlow<Map<String, List<Component>>>(emptyMap())
    val cardItems: StateFlow<Map<String, List<Component>>> = _cardItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun getServerUrl(): String? = serverConfigStore.currentServer.value?.serverApiUrl

    @Suppress("unused")
    // Existing generic fetch by link
    fun fetchCardItems(link: String, force: Boolean = false) {
        val serverUrl = getServerUrl() ?: run {
            _error.value = "No server selected"
            return
        }

        if (!force && _cardItems.value.containsKey(link)) return

        scope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getCardItems(serverUrl, link).collect { result ->
                result.fold(
                    onSuccess = { items ->
                        _cardItems.update { it + (link to items) }
                    },
                    onFailure = {
                        _error.value = it.message ?: "Failed to fetch items for $link"
                    }
                )
                _isLoading.value = false
            }
        }
    }

    // New: fetch paginated letter-based cards (always use letter API)
    fun fetchCards(type: String, char: Char = '_', force: Boolean = false) {
        val serverUrl = getServerUrl() ?: run {
            _error.value = "No server selected"
            return
        }


        val link = "music/$type/$char"

        if (!force && _cardItems.value.containsKey(link)) return

        val char = if (char == '#') '_' else char

        scope.launch {
            _isLoading.value = true
            _error.value = null

            repository.fetchCards(serverUrl, type, char).collect { result ->
                result.fold(
                    onSuccess = { items ->
                        _cardItems.update { it + (link to items) }
                    },
                    onFailure = { _error.value = it.message ?: "Failed to fetch items for $link" }
                )
                _isLoading.value = false
            }
        }
    }

    fun clearData() {
        _cardItems.value = emptyMap()
        _error.value = null
        _isLoading.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
