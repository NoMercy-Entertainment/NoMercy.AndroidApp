package tv.nomercy.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.nomercy.app.api.models.Server
import tv.nomercy.app.store.AppConfigStore
import tv.nomercy.app.store.AuthStore
import tv.nomercy.app.store.GlobalStores
import tv.nomercy.app.auth.AuthService

/**
 * ViewModel for managing user data and app configuration
 * Uses global stores - no prop drilling needed!
 */
class UserViewModel(
    private val context: Context
) : ViewModel() {

    // Access singleton global stores - ensures same instances everywhere!
    private val authStore = GlobalStores.getAuthStore(context)
    private val appConfigStore = GlobalStores.getAppConfigStore(context)

    // Expose store state flows directly
    val userProfile = appConfigStore.userProfile
    val servers = appConfigStore.servers
    val currentServer = appConfigStore.currentServer
    val messages = appConfigStore.messages
    val notifications = appConfigStore.notifications
    val isLoading = appConfigStore.isLoading
    val userInfo = authStore.userInfo
    val isAuthenticated = authStore.isAuthenticated

    // UI state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _needsServerSelection = MutableStateFlow(false)
    val needsServerSelection: StateFlow<Boolean> = _needsServerSelection.asStateFlow()

    private val _noServersAvailable = MutableStateFlow(false)
    val noServersAvailable: StateFlow<Boolean> = _noServersAvailable.asStateFlow()

    /**
     * Initialize user data after successful login
     * This should be called from the AuthViewModel after authentication succeeds
     */
    fun initializeUserData() {
        viewModelScope.launch {
            val result = appConfigStore.fetchAppConfig()

            result.fold(
                onSuccess = { appConfig ->
                    _error.value = null
                    handleServerSelectionState(appConfig.servers)
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load user data"
                }
            )
        }
    }

    /**
     * Refresh user data
     */
    fun refreshData() {
        viewModelScope.launch {
            val result = appConfigStore.refresh()

            result.fold(
                onSuccess = { appConfig ->
                    _error.value = null
                    handleServerSelectionState(appConfig.servers)
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to refresh data"
                }
            )
        }
    }

    /**
     * Select a server
     */
    fun selectServer(server: Server) {
        appConfigStore.setCurrentServer(server)
        _needsServerSelection.value = false
        _noServersAvailable.value = false
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear all user data (for logout)
     */
    fun clearUserData() {
        appConfigStore.clearData()
        authStore.clearAuth()
        _error.value = null
        _needsServerSelection.value = false
        _noServersAvailable.value = false
    }

    /**
     * Get server API client for making requests to the current server
     */
    fun getServerApiClient() = appConfigStore.getServerApiClient()

    private fun handleServerSelectionState(servers: List<Server>) {
        when {
            servers.isEmpty() -> {
                _noServersAvailable.value = true
                _needsServerSelection.value = false
            }
            servers.size == 1 -> {
                // Single server - auto-selected by store
                _noServersAvailable.value = false
                _needsServerSelection.value = false
            }
            servers.size > 1 && appConfigStore.currentServer.value == null -> {
                // Multiple servers but none selected
                _noServersAvailable.value = false
                _needsServerSelection.value = true
            }
            else -> {
                // Server already selected
                _noServersAvailable.value = false
                _needsServerSelection.value = false
            }
        }
    }
}

class UserViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
