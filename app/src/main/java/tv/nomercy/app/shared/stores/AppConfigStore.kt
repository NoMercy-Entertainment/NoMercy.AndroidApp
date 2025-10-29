package tv.nomercy.app.shared.stores

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.nomercy.app.shared.api.KeycloakConfig.getSuffix
import tv.nomercy.app.shared.api.services.DomainApiService
import tv.nomercy.app.shared.models.AppConfig
import tv.nomercy.app.shared.models.Message
import tv.nomercy.app.shared.models.Notification
import tv.nomercy.app.shared.models.Server
import tv.nomercy.app.shared.models.UserProfile
import tv.nomercy.app.shared.ui.ThemeName

class AppConfigStore(
    private val context: Context,
    private val authStore: AuthStore,
    private val themeDataStore: ThemeDataStore
) {
    private val domainApiClient = GlobalStores.getDomainApiClient(context)
    private val domainApiService = domainApiClient.createService<DomainApiService>()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers = _servers.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _features = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val features = _features.asStateFlow()

    private val _locale = MutableStateFlow<String?>(null)
    val locale = _locale.asStateFlow()

    private val _isModerator = MutableStateFlow(false)
    val isModerator = _isModerator.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin = _isAdmin.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _useAutoThemeColors = MutableStateFlow(true)
    val useAutoThemeColors = _useAutoThemeColors.asStateFlow()

    private var _isInitialized = false
    val isInitialized: Boolean get() = _isInitialized

    val tmdbApiKey : String
        get() = "ed3bf860adef0537783e4abee86d65af"

    fun getTheme(): Flow<ThemeName> = themeDataStore.getTheme

    suspend fun setTheme(themeName: ThemeName) {
        themeDataStore.setTheme(themeName)
    }

    suspend fun fetchAppConfig(): Result<AppConfig> {
        if (_isInitialized) return Result.success(createAppConfigFromState())

        _isLoading.value = true
        return try {
            val hasValidAuth = authStore.isAuthenticated.value && authStore.accessToken.value != null
            if (!hasValidAuth && !authStore.loadSavedTokens()) {
                return Result.failure(Exception("No authentication token available. Please log in first."))
            }

            val response = domainApiService.getAppConfig()
            val appConfig = response.body()?.data ?:
                return Result.failure(Exception(response.errorBody() ?.string() ?: "Unknown error"))

            updateStateFromAppConfig(appConfig)
            _isInitialized = true
            Result.success(appConfig)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    fun clearData() {
        _userProfile.value = null
        _messages.value = emptyList()
        _notifications.value = emptyList()
        _features.value = emptyMap()
        _locale.value = null
        _isModerator.value = false
        _isAdmin.value = false
        _isInitialized = false
    }

    private fun updateStateFromAppConfig(appConfig: AppConfig) {
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

        _messages.value = appConfig.messages ?: emptyList()
        _notifications.value = appConfig.notifications ?: emptyList()
        _features.value = appConfig.features ?: emptyMap()
        _locale.value = appConfig.locale
        _isModerator.value = appConfig.moderator ?: false
        _isAdmin.value = appConfig.admin ?: false
        _servers.value = appConfig.servers

        authStore.updateUserFromAppConfig(
            avatarUrl = appConfig.avatarUrl,
            locale = appConfig.locale,
            moderator = appConfig.moderator,
            admin = appConfig.admin
        )
    }

    private fun createAppConfigFromState(): AppConfig {
        return AppConfig(
            servers = _servers.value, // now handled elsewhere
            messages = _messages.value,
            notifications = _notifications.value,
            locale = _locale.value,
            name = _userProfile.value?.name,
            avatarUrl = _userProfile.value?.avatarUrl,
            features = _features.value,
            moderator = _isModerator.value,
            admin = _isAdmin.value
        )
    }
}