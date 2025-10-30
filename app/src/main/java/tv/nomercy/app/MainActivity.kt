package tv.nomercy.app

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import tv.nomercy.app.shared.auth.HandleAuthResponse
import tv.nomercy.app.shared.layout.SharedMainScreen
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.stores.updateLocale
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.ui.NoMercyTheme
import tv.nomercy.app.shared.ui.SystemUiController
import tv.nomercy.app.shared.ui.ThemeOverrideManager
import tv.nomercy.app.views.base.auth.shared.AuthViewModel
import tv.nomercy.app.views.base.auth.shared.AuthViewModelFactory
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.nomercy.app.components.ThemeManager
import tv.nomercy.app.views.profile.themeColors

class MainActivity : ComponentActivity() {
    private lateinit var insetsController: WindowInsetsControllerCompat
    private var isImmersiveState by mutableStateOf(false)
    private var exitConfirmed = false

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

    private var authResponseCallback: ((AuthorizationResponse?, AuthorizationException?) -> Unit)? =
        null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }

        // Handle any incoming intent that may contain the AppAuth redirect immediately.
        handleAuthIntent(intent)

        // Detect whether we're running on a TV device so we can choose the right UI and behavior.
        val isTvDevice = try {
            packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
                    (resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION
        } catch (_: Exception) {
            false
        }

        SystemUiController.setEdgeToEdge(this)
        // Lock portrait on phones only; TVs should not force orientation
        if (!isTvDevice) {
            SystemUiController.lockOrientationPortrait(this)
        }

        insetsController = WindowInsetsControllerCompat(window, window.decorView)

        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
            val appConfigStore = GlobalStores.getAppConfigStore(context)

            val themeOverrideManager = remember { ThemeOverrideManager() }

            val store = GlobalStores.getAppSettingsStore(this)
            val language by store.language.collectAsState(initial = "English")
            LaunchedEffect(language) {
                context.updateLocale(language)
            }

            LaunchedEffect(Unit) {
                appConfigStore.getTheme().collect { currentTheme ->
                    ThemeManager.currentThemeColor = themeColors.getValue(currentTheme)
                }
            }

            CompositionLocalProvider(
                LocalActivity provides this,
                LocalThemeOverrideManager provides themeOverrideManager
            ) {

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
                        platform = if (isTvDevice) Platform.TV else Platform.Mobile,
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

            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())

            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
        isImmersiveState = enabled
    }
}