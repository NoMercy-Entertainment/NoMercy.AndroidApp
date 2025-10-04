package tv.nomercy.app.mobile.entrypoint

import HandleAuthResponse
import SharedMainScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import tv.nomercy.app.Platform
import tv.nomercy.app.mobile.screens.auth.AuthViewModel
import tv.nomercy.app.mobile.screens.auth.AuthViewModelFactory
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.NoMercyTheme

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
        super.onCreate(savedInstanceState)
        setContent {
            NoMercyTheme {
                Surface(modifier = Modifier, color = MaterialTheme.colorScheme.background) {
                    val context = LocalContext.current
                    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
                    val appConfigStore = GlobalStores.getAppConfigStore(context)

                    HandleAuthResponse(authViewModel) { intent ->
                        authResponseCallback = { response, exception ->
                            authViewModel.handleAuthorizationResponse(response, exception)
                        }
                        authLauncher.launch(intent)
                    }

                    SharedMainScreen(
                        platform = Platform.Mobile,
                        authViewModel = authViewModel,
                        appConfigStore = appConfigStore
                    )
                }
            }
        }
    }
}