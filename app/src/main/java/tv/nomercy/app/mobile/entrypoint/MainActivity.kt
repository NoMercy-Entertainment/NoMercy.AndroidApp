package tv.nomercy.app.mobile.entrypoint

import HandleAuthResponse
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import tv.nomercy.app.Platform
import tv.nomercy.app.mobile.screens.auth.AuthViewModel
import tv.nomercy.app.mobile.screens.auth.AuthViewModelFactory
import tv.nomercy.app.shared.layout.SharedMainScreen
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.SystemUiController
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.ui.NoMercyTheme
import tv.nomercy.app.shared.ui.ThemeOverrideManager

class MainActivity : ComponentActivity() {
    private lateinit var insetsController: WindowInsetsControllerCompat
    private var isImmersiveState by mutableStateOf(false)

    // Pending response/exception when activity receives the redirect before the composable sets the callback
    private var pendingResponse: AuthorizationResponse? = null
    private var pendingException: AuthorizationException? = null

    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data == null) {
            // No intent data returned - treat as cancelled / no-op
            authResponseCallback?.invoke(null, null)
            authResponseCallback = null
            return@registerForActivityResult
        }
        val response = AuthorizationResponse.fromIntent(data)
        val exception = AuthorizationException.fromIntent(data)
        authResponseCallback?.invoke(response, exception)
        authResponseCallback = null
    }

    private var authResponseCallback: ((AuthorizationResponse?, AuthorizationException?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }

        super.onCreate(savedInstanceState)

        // Handle any incoming intent that may contain the AppAuth redirect immediately.
        handleAuthIntent(intent)

        SystemUiController.setEdgeToEdge(this)
        SystemUiController.lockOrientationPortrait(this)

        insetsController = WindowInsetsControllerCompat(window, window.decorView)

        setContent {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
            val appConfigStore = GlobalStores.getAppConfigStore(context)

            val themeOverrideManager = remember { ThemeOverrideManager() }

            CompositionLocalProvider(LocalActivity provides this, LocalThemeOverrideManager provides themeOverrideManager) {

                NoMercyTheme {
                    HandleAuthResponse(authViewModel) { intent ->
                        authResponseCallback = { response, exception ->
                            authViewModel.handleAuthorizationResponse(response, exception)
                        }

                        // If we already received the redirect earlier, flush it to the newly-set callback.
                        if (pendingResponse != null || pendingException != null) {
                            authResponseCallback?.invoke(pendingResponse, pendingException)
                            pendingResponse = null
                            pendingException = null
                        }

                        authLauncher.launch(intent)
                    }

                    SharedMainScreen(
                        platform = Platform.Mobile,
                        authViewModel = authViewModel,
                        appConfigStore = appConfigStore,
                        isImmersiveState = isImmersiveState,
                    )

//                    if (!isReady.value) {
//                        ThemedSplashScreen()
//                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthIntent(intent)
    }

    private fun handleAuthIntent(intent: Intent?) {
        if (intent == null) return
        val response = AuthorizationResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)
        if (response != null || exception != null) {
            if (authResponseCallback != null) {
                authResponseCallback?.invoke(response, exception)
                authResponseCallback = null
            } else {
                // Store it until the composable sets the callback
                pendingResponse = response
                pendingException = exception
            }
        }
    }

    override fun setImmersive(enabled: Boolean) {
        if (enabled) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())

            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())

            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
        isImmersiveState = enabled
    }
}
