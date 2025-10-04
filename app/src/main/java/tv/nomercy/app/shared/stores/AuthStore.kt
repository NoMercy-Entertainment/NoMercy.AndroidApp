package tv.nomercy.app.shared.stores

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.openid.appauth.AuthState as AppAuthState
import tv.nomercy.app.shared.api.services.UserInfo

/**
 * Pure Keycloak authentication store
 * Handles only authentication state and tokens
 */
class AuthStore(private val context: Context) {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Set authentication tokens
     */
    fun setTokens(accessToken: String, refreshToken: String? = null) {
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
        _isAuthenticated.value = true
        saveTokensToPrefs(accessToken, refreshToken)
    }

    /**
     * Set user info from token claims
     */
    fun setUserInfo(userInfo: UserInfo) {
        _userInfo.value = userInfo
    }

    /**
     * Update authentication loading state
     */
    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    /**
     * Save tokens and user info (used by AuthService)
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        idToken: String,
        userInfo: UserInfo
    ) {
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
        _userInfo.value = userInfo
        _isAuthenticated.value = true

        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putString("id_token", idToken)
            putString("user_id", userInfo.id)
            putString("username", userInfo.username)
            putString("email", userInfo.email)
            putString("avatar_url", userInfo.avatarUrl)
            putString("locale", userInfo.locale)
            putBoolean("moderator", userInfo.moderator)
            putBoolean("admin", userInfo.admin)
            apply()
        }
    }

    /**
     * Save auth state (used by AuthService)
     */
    fun saveAuthState(authState: AppAuthState) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("auth_state", authState.jsonSerializeString()).apply()
    }

    /**
     * Get auth state (used by AuthService)
     */
    fun getAuthState(): AppAuthState? {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val authStateJson = sharedPrefs.getString("auth_state", null)
        return authStateJson?.let { AppAuthState.jsonDeserialize(it) }
    }

    /**
     * Clear all authentication data
     */
    suspend fun clearTokens() {
        clearAuth()
    }

    /**
     * Clear all authentication data
     */
    fun clearAuth() {
        _isAuthenticated.value = false
        _userInfo.value = null
        _accessToken.value = null
        _refreshToken.value = null
        clearTokensFromPrefs()
    }

    /**
     * Load saved tokens from SharedPreferences
     */
    fun loadSavedTokens(): Boolean {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val savedAccessToken = sharedPrefs.getString("access_token", null)
        val savedRefreshToken = sharedPrefs.getString("refresh_token", null)
        val userId = sharedPrefs.getString("user_id", null)
        val username = sharedPrefs.getString("username", null)
        val email = sharedPrefs.getString("email", null)
        val avatarUrl = sharedPrefs.getString("avatar_url", null)
        val locale = sharedPrefs.getString("locale", null)
        val moderator = sharedPrefs.getBoolean("moderator", false)
        val admin = sharedPrefs.getBoolean("admin", false)

        return if (savedAccessToken != null && userId != null && username != null) {
            _accessToken.value = savedAccessToken
            _refreshToken.value = savedRefreshToken
            _userInfo.value = UserInfo(
                id = userId,
                username = username,
                email = email ?: "",
                avatarUrl = avatarUrl ?: "",
                locale = locale,
                moderator = moderator,
                admin = admin
            )
            _isAuthenticated.value = true
            true
        } else {
            false
        }
    }

    /**
     * Save tokens to SharedPreferences
     */
    private fun saveTokensToPrefs(accessToken: String, refreshToken: String?) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("access_token", accessToken)
            refreshToken?.let { putString("refresh_token", it) }
            apply()
        }
    }

    /**
     * Clear tokens from SharedPreferences
     */
    private fun clearTokensFromPrefs() {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
    }

    /**
     * Update user data from app_config
     */
    fun updateUserFromAppConfig(avatarUrl: String?, locale: String?, moderator: Boolean?, admin: Boolean?) {
        val currentUser = _userInfo.value
        if (currentUser != null) {
            _userInfo.value = currentUser.copy(
                avatarUrl = avatarUrl ?: currentUser.avatarUrl,
                locale = locale ?: currentUser.locale,
                moderator = moderator ?: currentUser.moderator,
                admin = admin ?: currentUser.admin
            )

            // Also update SharedPreferences
            val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().apply {
                avatarUrl?.let { putString("avatar_url", it) }
                putString("locale", locale ?: currentUser.locale)
                putBoolean("moderator", moderator ?: currentUser.moderator)
                putBoolean("admin", admin ?: currentUser.admin)
                apply()
            }
        }
    }
}
