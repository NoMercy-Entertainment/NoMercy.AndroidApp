package tv.nomercy.app.mobile.entrypoint

import HandleAuthResponse
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import tv.nomercy.app.Platform
import tv.nomercy.app.mobile.screens.auth.AuthViewModel
import tv.nomercy.app.mobile.screens.auth.AuthViewModelFactory
import tv.nomercy.app.shared.layout.SharedMainScreen
import tv.nomercy.app.shared.layout.ThemedSplashScreen
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.SystemUiController
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.ui.NoMercyTheme
import tv.nomercy.app.shared.ui.ThemeOverrideManager

class MainActivity : ComponentActivity() {
    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data!!
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

        SystemUiController.setEdgeToEdge(this)
        SystemUiController.lockOrientationPortrait(this)

        setContent {
            val context = LocalContext.current
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
            val appConfigStore = GlobalStores.getAppConfigStore(context)
            val authStore = GlobalStores.getAuthStore(context)
            val isReady = authStore.isReady.collectAsState()

            val themeOverrideManager = remember { ThemeOverrideManager() }

            CompositionLocalProvider(LocalActivity provides this, LocalThemeOverrideManager provides themeOverrideManager) {

                NoMercyTheme {
                    HandleAuthResponse(authViewModel) { intent ->
                        authResponseCallback = { response, exception ->
                            authViewModel.handleAuthorizationResponse(response, exception)
                        }
                        authLauncher.launch(intent)
                    }

                    SharedMainScreen(
                        platform = Platform.Mobile,
                        authViewModel = authViewModel,
                        appConfigStore = appConfigStore,
                    )

                    if (!isReady.value) {
                        ThemedSplashScreen()
                    }
                }
            }
        }
    }
}
