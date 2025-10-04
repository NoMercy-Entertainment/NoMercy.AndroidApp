package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.nomercy.app.shared.api.ServerApiClient
import tv.nomercy.app.shared.models.AppConfig
import tv.nomercy.app.shared.models.Message
import tv.nomercy.app.shared.models.Notification
import tv.nomercy.app.shared.models.Server
import tv.nomercy.app.shared.models.UserProfile
import tv.nomercy.app.shared.api.services.DomainApiService
import tv.nomercy.app.shared.api.services.ServerApiService
import tv.nomercy.app.shared.api.services.AuthService

/**
 * Pure app configuration store
 * Handles servers, user profile, messages, and notifications
 */
class AppConfigStore(
    private val context: Context,
    private val authService: AuthService,
    private val authStore: AuthStore // Accept shared AuthStore instance
) {

    private val domainApiClient = GlobalStores.getDomainApiClient(context)
    private val domainApiService = domainApiClient.createService<DomainApiService>()

    // App configuration state
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

    // Add missing fields from AppConfig
    private val _features = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val features: StateFlow<Map<String, Boolean>> = _features.asStateFlow()

    private val _locale = MutableStateFlow<String?>(null)
    val locale: StateFlow<String?> = _locale.asStateFlow()

    private val _isModerator = MutableStateFlow<Boolean>(false)
    val isModerator: StateFlow<Boolean> = _isModerator.asStateFlow()

    private val _isAdmin = MutableStateFlow<Boolean>(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var _isInitialized = false
    val isInitialized: Boolean get() = _isInitialized

    /**
     * Fetch app configuration from the API
     */
    suspend fun fetchAppConfig(): Result<AppConfig> {
        if (_isInitialized) {
            return Result.success(createAppConfigFromState())
        }

        _isLoading.value = true

        return try {
            // First, ensure we have a valid authentication token
            val hasValidAuth = authStore.isAuthenticated.value && authStore.accessToken.value != null
            if (!hasValidAuth) {
                // Try to load saved tokens first
                val tokenLoaded = authStore.loadSavedTokens()
                if (!tokenLoaded) {
                    return Result.failure(Exception("No authentication token available. Please log in first."))
                }
            }

            val response = domainApiService.getAppConfig()

            if (response.isSuccessful && response.body()?.data != null) {
                val appConfig = response.body()!!.data!!
                updateStateFromAppConfig(appConfig)
                handleServerSelection()
                _isInitialized = true
                Result.success(appConfig)
            } else if (response.code() == 401) {
                // Handle authentication failure specifically
                authStore.clearAuth()
                Result.failure(Exception("Authentication failed. Please log in again."))
            } else {
                Result.failure(Exception("Failed to fetch app config: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Set the current server and save preference
     */
    fun setCurrentServer(server: Server) {
        println("DEBUG AppConfigStore: setCurrentServer called")
        println("DEBUG AppConfigStore: Server ID: ${server.id}")
        println("DEBUG AppConfigStore: Server Name: ${server.name}")
        println("DEBUG AppConfigStore: Server URL: '${server.serverApiUrl}'")

        _currentServer.value = server
        saveSelectedServerId(server.id)

        // Immediately fetch libraries when server is set
        println("DEBUG AppConfigStore: Server set, triggering library fetch...")
        val libraryStore = GlobalStores.getLibraryStore(context)
        libraryStore.fetchLibraries()
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
     * Fetch server permissions for the current server
     */
    suspend fun fetchServerPermissions(): Result<Unit> {
        val currentServerValue = _currentServer.value ?: return Result.success(Unit)

        return try {
            val serverApiClient = getServerApiClient() ?: return Result.failure(
                Exception("No server API client available")
            )

            val serverApiService = serverApiClient.createService<ServerApiService>()
            val response = serverApiService.getServerPermissions()

            if (response.isSuccessful && response.body()?.data != null) {
                val permissions = response.body()!!.data!!
                updateServerPermissions(currentServerValue, permissions.owner, permissions.manager)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to fetch server permissions: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Server permissions request failed: ${e.message}"))
        }
    }

    /**
     * Clear all app configuration data
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
     * Refresh app configuration
     */
    suspend fun refresh(): Result<AppConfig> {
        _isInitialized = false
        return fetchAppConfig()
    }

    // Private helper methods
    private fun updateStateFromAppConfig(appConfig: AppConfig) {
        _servers.value = appConfig.servers
        _messages.value = appConfig.messages ?: emptyList()
        _notifications.value = appConfig.notifications ?: emptyList()

        _userProfile.value = UserProfile(
            id = "",
            name = appConfig.name ?: "",
            email = "",
            avatarUrl = appConfig.avatarUrl,
            locale = appConfig.locale,
            features = appConfig.features,
            moderator = appConfig.moderator,
            admin = appConfig.admin
        )

        // Update the AuthStore with all user data from app_config
        authStore.updateUserFromAppConfig(
            avatarUrl = appConfig.avatarUrl,
            locale = appConfig.locale,
            moderator = appConfig.moderator,
            admin = appConfig.admin
        )

        // Update missing fields
        _features.value = appConfig.features ?: emptyMap()
        _locale.value = appConfig.locale
        _isModerator.value = appConfig.moderator ?: false
        _isAdmin.value = appConfig.admin ?: false
    }

    private fun createAppConfigFromState(): AppConfig {
        return AppConfig(
            servers = _servers.value,
            messages = _messages.value,
            notifications = _notifications.value,
            locale = _userProfile.value?.locale,
            name = _userProfile.value?.name,
            avatarUrl = _userProfile.value?.avatarUrl,
            features = _userProfile.value?.features,
            moderator = _userProfile.value?.moderator,
            admin = _userProfile.value?.admin
        )
    }

    private fun handleServerSelection() {
        val servers = _servers.value

        when {
            servers.isEmpty() -> {
                // No servers available
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
                }
            }
        }
    }

    private fun updateServerPermissions(server: Server, isOwner: Boolean, isManager: Boolean) {
        val updatedServer = server.copy(
            isOwner = isOwner,
            isManager = isManager
        )

        _currentServer.value = updatedServer

        // Update the server in the servers list
        val updatedServersList = _servers.value.map { s ->
            if (s.id == updatedServer.id) updatedServer else s
        }
        _servers.value = updatedServersList
    }

    // Server ID persistence methods
    private fun saveSelectedServerId(serverId: String) {
        val sharedPrefs = context.getSharedPreferences("app_config_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("selected_server_id", serverId).apply()
    }

    private fun getSavedServerId(): String? {
        val sharedPrefs = context.getSharedPreferences("app_config_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("selected_server_id", null)
    }

    private fun clearSelectedServerId() {
        val sharedPrefs = context.getSharedPreferences("app_config_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().remove("selected_server_id").apply()
    }
}
