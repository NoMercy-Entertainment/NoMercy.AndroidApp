package tv.nomercy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.nomercy.app.auth.AuthViewModel
import tv.nomercy.app.auth.AuthViewModelFactory
import tv.nomercy.app.ui.phone.MobileMainScreen
import tv.nomercy.app.ui.shared.NoMercyTheme

class MainActivity : ComponentActivity() {

    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val authResponse = AuthorizationResponse.fromIntent(data)
            val authException = AuthorizationException.fromIntent(data)

            // This will be handled by the AuthViewModel
            authResponseReceived(authResponse, authException)
        }
    }

    private var authResponseCallback: ((AuthorizationResponse?, AuthorizationException?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoMercyTheme {
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))

                    // Observe login intent
                    val loginIntent by authViewModel.loginIntent.collectAsState()

                    LaunchedEffect(loginIntent) {
                        loginIntent?.let { intent ->
                            authResponseCallback = { response, exception ->
                                authViewModel.handleAuthorizationResponse(response, exception)
                            }
                            authLauncher.launch(intent.intent)
                            authViewModel.clearLoginIntent()
                        }
                    }

                    MobileMainScreen()
                }
            }
        }
    }

    private fun authResponseReceived(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?
    ) {
        authResponseCallback?.invoke(authResponse, authException)
        authResponseCallback = null
    }
}
