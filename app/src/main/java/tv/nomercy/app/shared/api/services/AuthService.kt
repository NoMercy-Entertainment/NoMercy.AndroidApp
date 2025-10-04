package tv.nomercy.app.shared.api.services

import android.content.Context
import android.content.Intent
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.*
import net.openid.appauth.AuthState as AppAuthState
import org.json.JSONObject
import tv.nomercy.app.shared.api.KeycloakConfig
import tv.nomercy.app.shared.stores.AuthStore
import kotlin.coroutines.resume

class AuthService(
    private val context: Context,
    private val authStore: AuthStore
) {
    private var authService: AuthorizationService? = null
    private var authState: AppAuthState? = null

    private fun getAuthService(): AuthorizationService {
        if (authService == null) {
            authService = KeycloakConfig.createAuthService(context)
        }
        return authService!!
    }

    fun login(): AuthResult {
        return try {
            val authRequest = KeycloakConfig.createAuthorizationRequest()
            val authIntent = getAuthService().getAuthorizationRequestIntent(authRequest)

            // This will be handled by the calling Activity
            AuthResult.LoginIntent(authIntent, authRequest)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun handleAuthorizationResponse(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?
    ): AuthResult {
        return suspendCancellableCoroutine { continuation ->
            when {
                authException != null -> {
                    continuation.resume(AuthResult.Error(authException.message ?: "Authorization failed"))
                }
                authResponse != null -> {
                    val tokenRequest = authResponse.createTokenExchangeRequest()

                    getAuthService().performTokenRequest(tokenRequest) { tokenResponse, tokenException ->
                        if (tokenException != null) {
                            continuation.resume(AuthResult.Error(tokenException.message ?: "Token exchange failed"))
                        } else if (tokenResponse != null) {
                            // Update auth state
                            authState = AppAuthState(authResponse, tokenResponse, tokenException)

                            // Extract user info from ID token
                            val userInfo = extractUserInfoFromIdToken(tokenResponse.idToken)

                            // Save tokens synchronously to ensure they're available immediately
                            runBlocking {
                                saveTokens(
                                    accessToken = tokenResponse.accessToken ?: "",
                                    refreshToken = tokenResponse.refreshToken ?: "",
                                    idToken = tokenResponse.idToken ?: "",
                                    userInfo = userInfo
                                )
                            }

                            continuation.resume(
                                AuthResult.Success(
                                    accessToken = tokenResponse.accessToken ?: "",
                                    user = userInfo
                                )
                            )
                        }
                    }
                }
                else -> {
                    continuation.resume(AuthResult.Error("No authorization response received"))
                }
            }
        }
    }

    suspend fun logout(): Boolean {
        return try {
            authService?.dispose()
            authService = null
            authState = null
            clearTokens()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun refreshToken(): Boolean {
        return try {
            val currentAuthState = authState
            if (currentAuthState?.refreshToken != null) {
                suspendCancellableCoroutine { continuation ->
                    getAuthService().performTokenRequest(
                        currentAuthState.createTokenRefreshRequest()
                    ) { tokenResponse, tokenException ->
                        if (tokenException != null || tokenResponse == null) {
                            continuation.resume(false)
                        } else if (tokenResponse != null) {
                            // Update auth state
                            currentAuthState.update(tokenResponse, tokenException)
                            authState = currentAuthState

                            // Extract user info and save tokens
                            val userInfo = extractUserInfoFromIdToken(tokenResponse.idToken)
                            CoroutineScope(Dispatchers.IO).launch {
                                saveTokens(
                                    accessToken = tokenResponse.accessToken ?: "",
                                    refreshToken = tokenResponse.refreshToken ?: currentAuthState.refreshToken ?: "",
                                    idToken = tokenResponse.idToken ?: "",
                                    userInfo = userInfo
                                )
                            }

                            continuation.resume(true)
                        }
                    }
                }
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun isLoggedIn(): Boolean {
        // Check both AppAuth state and AuthStore tokens
        val hasAppAuthState = authState?.isAuthorized == true
        val hasStoredTokens = authStore.isAuthenticated.value && authStore.accessToken.value != null
        return hasAppAuthState || hasStoredTokens
    }

    private fun extractUserInfoFromIdToken(idToken: String?): UserInfo {
        return try {
            if (idToken != null) {
                // Decode JWT payload (simple base64 decode for demo - in production use proper JWT library)
                val parts = idToken.split(".")
                if (parts.size >= 2) {
                    val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                    val json = JSONObject(payload)

                    UserInfo(
                        id = json.optString("sub", ""),
                        username = json.optString("preferred_username", ""),
                        email = json.optString("email", ""),
                        avatarUrl = "", // avatarUrl comes from app_config, not Keycloak
                        locale = json.optString("locale", "en-US"),
                        moderator = json.optBoolean("moderator", false),
                        admin = json.optBoolean("admin", false)
                    )
                } else {
                    UserInfo("", "", "", "", null, false, false)
                }
            } else {
                UserInfo("", "", "", "", null, false, false)
            }
        } catch (_: Exception) {
            UserInfo("", "", "", "", null, false, false)
        }
    }

    private suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        idToken: String,
        userInfo: UserInfo
    ) {
        authStore.saveTokens(accessToken, refreshToken, idToken, userInfo)
    }

    private suspend fun clearTokens() {
        authStore.clearTokens()
    }

    suspend fun restoreAuthState() {
        try {
            // First restore the AppAuth state
            authState = authStore.getAuthState()

            // If no AppAuth state but we have tokens, create a minimal auth state
            if (authState == null && authStore.isAuthenticated.value) {
                // We have tokens but no AppAuth state - this is common after app restart
                // The user is still considered authenticated via stored tokens
            }
        } catch (_: Exception) {
            // Handle restore error
        }
    }

    /**
     * Load saved tokens from SharedPreferences and update AuthStore
     */
    suspend fun loadSavedTokens(): Boolean {
        val tokensLoaded = authStore.loadSavedTokens()

        // If tokens were loaded successfully, try to restore the AppAuth state too
        if (tokensLoaded) {
            restoreAuthState()
        }

        return tokensLoaded
    }

    fun getUserInfo(): Flow<UserInfo?> {
        return authStore.userInfo
    }
}

sealed class AuthResult {
    data class Success(val accessToken: String, val user: UserInfo) : AuthResult()
    data class Error(val message: String) : AuthResult()
    data class LoginIntent(val intent: Intent, val request: AuthorizationRequest) : AuthResult()
}

data class UserInfo(
    val id: String,
    val username: String,
    val email: String,
    val avatarUrl: String,
    val locale: String?,
    val moderator: Boolean,
    val admin: Boolean
)
