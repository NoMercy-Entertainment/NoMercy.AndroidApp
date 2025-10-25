package tv.nomercy.app.views.base.auth.shared

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import tv.nomercy.app.shared.api.DeviceAuthResponse
import tv.nomercy.app.shared.api.services.AuthResult
import tv.nomercy.app.shared.api.services.AuthService
import tv.nomercy.app.shared.api.services.UserInfo
import tv.nomercy.app.shared.stores.AuthStore

class AuthViewModel(
    private val authService: AuthService
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo.asStateFlow()

    private val _loginIntent = MutableStateFlow<AuthResult.LoginIntent?>(null)
    val loginIntent: StateFlow<AuthResult.LoginIntent?> = _loginIntent.asStateFlow()

    private var deviceAuthResponse: DeviceAuthResponse? = null
    // Job used to track and cancel the polling coroutine (prevents concurrent polls)
    private var pollingJob: Job? = null

    init {
        checkAuthStatus()
        observeUserInfo()
    }

    override fun onCleared() {
        authService.dispose()
        super.onCleared()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                // First restore the AppAuth state
                authService.restoreAuthState()

                // Then load saved tokens and update AuthStore
                val tokensLoaded = authService.loadSavedTokens()

                if (tokensLoaded && authService.isLoggedIn()) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (_: Exception) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    private fun observeUserInfo() {
        viewModelScope.launch {
            authService.getUserInfo().collect { userInfo ->
                _userInfo.value = userInfo
            }
        }
    }

    fun login() {
        // Cancel any existing polling to ensure a clean device flow
        pollingJob?.cancel()
        pollingJob = null
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authService.login()) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Authenticated
                    _userInfo.value = result.user
                    // Cancel any polling if present
                    pollingJob?.cancel()
                    pollingJob = null
                }

                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                    pollingJob?.cancel()
                    pollingJob = null
                }

                is AuthResult.LoginIntent -> {
                    _loginIntent.value = result
                    _authState.value = AuthState.AwaitingLogin
                }

                is AuthResult.TvLogin -> {
                    deviceAuthResponse = result.response
                    val expiresAt = System.currentTimeMillis() + (result.response.expiresIn * 1000L)
                    _authState.value = AuthState.TvInstructions(
                        result.response.verificationUri,
                        result.response.userCode,
                        expiresAt
                    )
                }
            }
        }
    }

    fun pollForToken() {
        // If a polling job is already active, don't start another
        if (pollingJob?.isActive == true) return

        // Launch and track the polling job so it can be cancelled externally
        pollingJob = viewModelScope.launch {
            try {
                var resp = deviceAuthResponse
                if (resp == null) {
                    _authState.value = AuthState.Error("No device auth response available")
                    return@launch
                }

                // compute expiresAt for the current response
                var expiresAt = System.currentTimeMillis() + (resp.expiresIn * 1000L)
                _authState.value = AuthState.TvPolling(resp.verificationUri, resp.userCode, expiresAt)

                var polling = true
                while (polling && isActive) {
                    // Re-evaluate expiration on each loop
                    val now = System.currentTimeMillis()

                    // If device code expired, request a new device code and restart polling
                    if (now >= (deviceAuthResponse?.let { expiresAt } ?: now)) {
                        // Request new device code by calling login(); this will update deviceAuthResponse
                        when (val newResult = authService.login()) {
                            is AuthResult.TvLogin -> {
                                deviceAuthResponse = newResult.response
                                resp = deviceAuthResponse ?: break
                                expiresAt = System.currentTimeMillis() + (resp.expiresIn * 1000L)
                                // update UI to show new code
                                _authState.value = AuthState.TvInstructions(resp.verificationUri, resp.userCode, expiresAt)
                                // continue to polling with new resp
                                continue
                            }

                            is AuthResult.Error -> {
                                _authState.value = AuthState.Error(newResult.message)
                                deviceAuthResponse = null
                                break
                            }

                            else -> {
                                _authState.value = AuthState.Error("Failed to refresh device code")
                                deviceAuthResponse = null
                                break
                            }
                        }
                    }

                    // Always read the latest deviceAuthResponse
                    val current = deviceAuthResponse ?: run {
                        _authState.value = AuthState.Error("Device auth cancelled")
                        break
                    }

                    when (val result = authService.pollForToken(current.deviceCode)) {
                        is AuthResult.Success -> {
                            _authState.value = AuthState.Authenticated
                            _userInfo.value = result.user
                            // clear device auth data to stop further polling
                            deviceAuthResponse = null
                            polling = false
                        }

                        is AuthResult.Error -> {
                            when (result.message) {
                                "pending" -> delay(current.interval * 1000L)
                                "slow down" -> delay((current.interval + 5) * 1000L)
                                else -> {
                                    _authState.value = AuthState.Error(result.message)
                                    // clear device auth data
                                    deviceAuthResponse = null
                                    polling = false
                                }
                            }
                        }

                        else -> {
                            // Should not happen
                            _authState.value = AuthState.Error("Unexpected result during polling")
                            deviceAuthResponse = null
                            polling = false
                        }
                    }
                }
            } finally {
                // Clear the tracked job so a new poll can be started later
                pollingJob = null
            }
        }
    }

    fun handleAuthorizationResponse(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authService.handleAuthorizationResponse(authResponse, authException)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Authenticated
                    _userInfo.value = result.user
                    // Cancel any polling job when an authorization response completes the flow
                    pollingJob?.cancel()
                    pollingJob = null
                }

                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                    pollingJob?.cancel()
                    pollingJob = null
                }

                is AuthResult.LoginIntent -> {
                    _authState.value = AuthState.Error("Unexpected login intent")
                }

                is AuthResult.TvLogin -> {
                    // This should not happen in this flow
                    _authState.value = AuthState.Error("Unexpected TV login result")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val success = authService.logout()
            if (success) {
                _authState.value = AuthState.Unauthenticated
                _userInfo.value = null
                // cancel any ongoing device-auth data/polling
                deviceAuthResponse = null
                pollingJob?.cancel()
                pollingJob = null
            } else {
                _authState.value = AuthState.Error("Logout failed")
            }
        }
    }

    fun refreshToken() {
        viewModelScope.launch {
            val success = authService.refreshToken()
            if (!success) {
                _authState.value = AuthState.Unauthenticated
                _userInfo.value = null
            }
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun clearLoginIntent() {
        _loginIntent.value = null
    }
}

class AuthViewModelFactory(
    private val context: Context,
    private val authStore: AuthStore? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val store = authStore ?: AuthStore(context)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(AuthService(context, store)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object AwaitingLogin : AuthState()
    data class TvInstructions(val verificationUri: String, val userCode: String, val expiresAt: Long) : AuthState()
    data class TvPolling(val verificationUri: String, val userCode: String, val expiresAt: Long) : AuthState()
    data class Error(val message: String) : AuthState()
}