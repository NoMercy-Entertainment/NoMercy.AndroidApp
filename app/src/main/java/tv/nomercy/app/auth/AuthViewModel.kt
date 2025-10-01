package tv.nomercy.app.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import tv.nomercy.app.ui.viewmodel.UserViewModel

class AuthViewModel(
    private val authService: AuthService
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo.asStateFlow()

    private val _loginIntent = MutableStateFlow<AuthResult.LoginIntent?>(null)
    val loginIntent: StateFlow<AuthResult.LoginIntent?> = _loginIntent.asStateFlow()

    // Add UserViewModel integration
    private var userViewModel: UserViewModel? = null

    init {
        checkAuthStatus()
        observeUserInfo()
    }

    fun setUserViewModel(userViewModel: UserViewModel) {
        this.userViewModel = userViewModel
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
            } catch (e: Exception) {
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
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authService.login()) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Authenticated
                    _userInfo.value = result.user
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is AuthResult.LoginIntent -> {
                    _loginIntent.value = result
                    _authState.value = AuthState.AwaitingLogin
                }
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
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is AuthResult.LoginIntent -> {
                    _authState.value = AuthState.Error("Unexpected login intent")
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
    private val authStore: tv.nomercy.app.store.AuthStore? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val store = authStore ?: tv.nomercy.app.store.AuthStore(context)
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
    data class Error(val message: String) : AuthState()
}
