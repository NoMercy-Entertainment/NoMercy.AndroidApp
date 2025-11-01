package tv.nomercy.app

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import tv.nomercy.app.components.ThemeManager
import tv.nomercy.app.shared.auth.HandleAuthResponse
import tv.nomercy.app.shared.layout.SharedMainScreen
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.stores.musicPlayer.MusicPlayerService
import tv.nomercy.app.shared.stores.updateLocale
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.ui.NoMercyTheme
import tv.nomercy.app.shared.ui.SystemUiController
import tv.nomercy.app.shared.ui.ThemeOverrideManager
import tv.nomercy.app.views.base.auth.shared.AuthViewModel
import tv.nomercy.app.views.base.auth.shared.AuthViewModelFactory
import tv.nomercy.app.views.profile.themeColors

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "NoMercyApplication"
    }

    private lateinit var insetsController: WindowInsetsControllerCompat
    private var isImmersiveState by mutableStateOf(false)
    private var exitConfirmed = false

    private var navController: NavHostController? = null

    private var pendingResponse: AuthorizationResponse? = null
    private var pendingException: AuthorizationException? = null

    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data == null) {
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

        handleAuthIntent(intent)

        val isTvDevice = try {
            packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
                    (resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION
        } catch (_: Exception) {
            false
        }

        SystemUiController.setEdgeToEdge(this)
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
                        onNavControllerReady = { nav ->
                            navController = nav
                        }
                    )
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

    override fun onDestroy() {
        try {
            val stopIntent = Intent(this, MusicPlayerService::class.java)
                .setAction(MusicPlayerService.ACTION_STOP_IF_INACTIVE)
            startService(stopIntent)
        } catch (t: Throwable) {
            Log.d("MusicPlayerService", "onDestroy: $t")
        }
        super.onDestroy()
    }
}