package tv.nomercy.app.mobile.screens.selectServer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.models.Server
import tv.nomercy.app.shared.stores.AppConfigStore
import tv.nomercy.app.shared.stores.ServerConfigStore

/**
 * ViewModel for handling app setup flow independent of authentication
 */
class SelectServerViewModel(
    private val appConfigStore: AppConfigStore,
    private val serverConfigStore: ServerConfigStore
) : ViewModel() {

    private val _setupState = MutableStateFlow<SetupState>(SetupState.CheckingConfiguration)
    val setupState: StateFlow<SetupState> = _setupState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Observe store data
    val currentServer = serverConfigStore.currentServer
    val userProfile = appConfigStore.userProfile

    init {
        observeSetupRequirements()
    }

    /**
     * Check what setup steps are needed
     */
    fun checkSetupRequirements() {
        viewModelScope.launch {
            _isLoading.value = true
            _setupState.value = SetupState.CheckingConfiguration

            val result = appConfigStore.fetchAppConfig()

            result.fold(
                onSuccess = {
                    determineSetupState()
                },
                onFailure = { exception ->
                    _setupState.value = SetupState.Error("Failed to load configuration: ${exception.message}")
                }
            )

            _isLoading.value = false
        }
    }

    /**
     * Select a server during setup
     */
    fun selectServer(server: Server) {
        serverConfigStore.setCurrentServer(server)

        // Fetch server permissions after selecting server
        viewModelScope.launch {
            val permissionsResult = serverConfigStore.fetchServerPermissions()
            permissionsResult.fold(
                onSuccess = {
                    determineSetupState()
                },
                onFailure = { exception ->
                    // If permissions fetch fails, it might indicate server is offline
                    _setupState.value = SetupState.Error(
                        message = "Server appears to be offline or unreachable: ${exception.message}",
                        canRetry = true
                    )
                }
            )
        }
    }

    /**
     * Skip server selection (if optional)
     */
    fun skipServerSelection() {
        _setupState.value = SetupState.Complete
    }

    /**
     * Retry failed operations
     */
    fun retry() {
        checkSetupRequirements()
    }

    /**
     * Reset setup to initial state
     */
    fun resetSetup() {
        appConfigStore.clearData()
        _setupState.value = SetupState.CheckingConfiguration
        checkSetupRequirements()
    }

    private fun observeSetupRequirements() {
        viewModelScope.launch {
            // Combine all relevant state to determine setup requirements
            combine(
                appConfigStore.servers,
                currentServer,
                userProfile
            ) { _, _, _ ->
                if (_setupState.value !is SetupState.CheckingConfiguration &&
                    _setupState.value !is SetupState.Error) {
                    determineSetupState()
                }
            }.collect {}
        }
    }

    private fun determineSetupState() {
        val serversList = appConfigStore.servers.value
        val selectedServer = currentServer.value

        when {
            // No servers available
            serversList.isEmpty() -> {
                _setupState.value = SetupState.NoServersAvailable
            }

            // Multiple servers but none selected
            serversList.size > 1 && selectedServer == null -> {
                _setupState.value = SetupState.ServerSelectionRequired(serversList)
            }

            // Single server - auto-select and validate
            serversList.size == 1 && selectedServer == null -> {
                val singleServer = serversList[0]
                serverConfigStore.setCurrentServer(singleServer)

                // Set loading state while validating server
                _setupState.value = SetupState.CheckingConfiguration

                // Validate server connectivity
                viewModelScope.launch {
                    _isLoading.value = true
                    val permissionsResult = serverConfigStore.fetchServerPermissions()
                    _isLoading.value = false

                    permissionsResult.fold(
                        onSuccess = {
                            _setupState.value = SetupState.Complete
                        },
                        onFailure = {
                            _setupState.value = SetupState.ServerOffline
                        }
                    )
                }
            }

            // Server selected, setup complete
            selectedServer != null -> {
                _setupState.value = SetupState.Complete
            }

            // Fallback
            else -> {
                _setupState.value = SetupState.Complete
            }
        }
    }
}

/**
 * Setup states independent of authentication
 */
sealed class SetupState {
    object CheckingConfiguration : SetupState()
    object NoServersAvailable : SetupState()
    data class ServerSelectionRequired(val servers: List<Server>) : SetupState()
    object ServerOffline : SetupState()
    object Complete : SetupState()
    data class Error(val message: String, val canRetry: Boolean = true) : SetupState()
}

class SetupViewModelFactory(
    private val appConfigStore: AppConfigStore,
    private val serverConfigStore: ServerConfigStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectServerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SelectServerViewModel(appConfigStore, serverConfigStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
