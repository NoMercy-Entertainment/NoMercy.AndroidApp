package tv.nomercy.app.api.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.nomercy.app.api.DomainApiClient
import tv.nomercy.app.api.ServerApiClient
import tv.nomercy.app.api.models.AppConfig
import tv.nomercy.app.api.models.Message
import tv.nomercy.app.api.models.Notification
import tv.nomercy.app.api.models.Server
import tv.nomercy.app.api.models.UserProfile
import tv.nomercy.app.api.services.DomainApiService
import tv.nomercy.app.api.services.ServerApiService
import tv.nomercy.app.auth.AuthService
import tv.nomercy.app.store.AuthStore

/**
 * Repository for managing user data, servers, and app configuration
 * Equivalent to the getLocations.ts middleware
 */
class UserRepository(
    private val context: Context,
    private val authService: AuthService,
    private val authStore: AuthStore
) {

    private val domainApiClient = DomainApiClient(context, authService, authStore)
    private val domainApiService = domainApiClient.createService<DomainApiService>()

    // State management similar to Vue.js stores
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers: StateFlow<List<Server>> = _servers.asStateFlow()

    private val _currentServer = MutableStateFlow<Server?>(null)
    val currentServer: StateFlow<Server?> = _currentServer.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var _isInitialized = false
    val isInitialized: Boolean get() = _isInitialized

    /**
     * Fetch app configuration including user info, servers, and permissions
     * Equivalent to the getLocations() function
     */
    suspend fun fetchAppConfig(): Result<AppConfig> {
        if (_isInitialized) {
            return Result.success(_servers.value.let { servers ->
                AppConfig(
                    servers = servers,
                    messages = _messages.value,
                    notifications = _notifications.value,
                    locale = _userProfile.value?.locale,
                    name = _userProfile.value?.name,
                    avatarUrl = _userProfile.value?.avatarUrl,
                    features = _userProfile.value?.features,
                    moderator = _userProfile.value?.moderator,
                    admin = _userProfile.value?.admin
                )
            })
        }

        _isLoading.value = true

        return try {
            val response = domainApiService.getAppConfig()

            if (response.isSuccessful && response.body()?.data != null) {
                val appConfig = response.body()!!.data!!

                // Update state with fetched data
                _servers.value = appConfig.servers
                _messages.value = appConfig.messages ?: emptyList()
                _notifications.value = appConfig.notifications ?: emptyList()

                // Update user profile
                _userProfile.value = UserProfile(
                    id = "", // Will be filled from auth service
                    name = appConfig.name ?: "",
                    email = "", // Will be filled from auth service
                    avatarUrl = appConfig.avatarUrl,
                    locale = appConfig.locale,
                    features = appConfig.features,
                    moderator = appConfig.moderator,
                    admin = appConfig.admin ?: false
                )

                _isInitialized = true

                // Handle server selection logic
                handleServerSelection()

                Result.success(appConfig)
            } else {
                Result.failure(Exception("Failed to fetch app config: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Handle server selection logic similar to the TypeScript version
     */
    private fun handleServerSelection() {
        val servers = _servers.value

        when {
            servers.isEmpty() -> {
                // No servers available - user needs to set up a server
                // This would trigger navigation to "No Servers" screen
            }
            servers.size == 1 -> {
                // Only one server - auto-select it
                setCurrentServer(servers[0])
            }
            servers.size > 1 -> {
                // Multiple servers - check for saved preference
                val savedServerId = getSavedServerId()
                val savedServer = servers.find { it.id == savedServerId }

                if (savedServer != null) {
                    setCurrentServer(savedServer)
                } else {
                    // No saved server - user needs to select one
                    // This would trigger navigation to "Select Server" screen
                }
            }
        }
    }

    /**
     * Set the current server and save preference
     */
    fun setCurrentServer(server: Server) {
        _currentServer.value = server
        saveSelectedServerId(server.id)
    }

    /**
     * Get server API client for the current server
     */
    fun getServerApiClient(): ServerApiClient? {
        return _currentServer.value?.let { server ->
            ServerApiClient.create(server.serverApiUrl, context, authService, authStore)
        }
    }

    /**
     * Clear all data (for logout)
     */
    fun clearData() {
        _userProfile.value = null
        _servers.value = emptyList()
        _currentServer.value = null
        _messages.value = emptyList()
        _notifications.value = emptyList()
        _isInitialized = false
        clearSelectedServerId()
    }

    /**
     * Refresh data
     */
    suspend fun refresh(): Result<AppConfig> {
        _isInitialized = false
        return fetchAppConfig()
    }

    /**
     * Fetch server permissions for the current server and update server state
     * Equivalent to the getServerPermissions() function from TypeScript
     */
    suspend fun fetchServerPermissions(): Result<Unit> {
        val currentServerValue = _currentServer.value

        if (currentServerValue == null) {
            return Result.success(Unit) // No server selected, nothing to do
        }

        return try {
            val serverApiClient = getServerApiClient() ?: return Result.failure(
                Exception("No server API client available")
            )

            val serverApiService = serverApiClient.createService<ServerApiService>()
            val response = serverApiService.getServerPermissions()

            if (response.isSuccessful && response.body()?.data != null) {
                val permissions = response.body()!!.data!!

                // Update current server with permissions data
                val updatedServer = currentServerValue.copy(
                    isOwner = permissions.owner,
                    isManager = permissions.manager
                )

                _currentServer.value = updatedServer

                // Also update the server in the servers list
                val updatedServersList = _servers.value.map { server ->
                    if (server.id == updatedServer.id) updatedServer else server
                }
                _servers.value = updatedServersList

                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to fetch server permissions: ${response.message()}"))
            }
        } catch (e: Exception) {
            // If permissions request fails, it might mean server is offline
            Result.failure(Exception("Server permissions request failed: ${e.message}"))
        }
    }

    // Server ID persistence methods
    private fun saveSelectedServerId(serverId: String) {
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("selected_server_id", serverId).apply()
    }

    private fun getSavedServerId(): String? {
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("selected_server_id", null)
    }

    private fun clearSelectedServerId() {
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().remove("selected_server_id").apply()
    }
}
