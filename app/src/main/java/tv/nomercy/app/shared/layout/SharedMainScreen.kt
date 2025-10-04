import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.nomercy.app.Platform
import tv.nomercy.app.mobile.screens.auth.AuthState
import tv.nomercy.app.mobile.screens.auth.AuthViewModel
import tv.nomercy.app.mobile.screens.auth.LoginScreen
import tv.nomercy.app.mobile.screens.selectServer.SelectServerScreen
import tv.nomercy.app.mobile.screens.selectServer.SelectServerViewModel
import tv.nomercy.app.mobile.screens.selectServer.SetupViewModelFactory
import tv.nomercy.app.shared.stores.AppConfigStore
import tv.nomercy.app.tv.layout.TVMainScreen

@Composable
fun SharedMainScreen(
    platform: Platform,
    authViewModel: AuthViewModel,
    appConfigStore: AppConfigStore
) {
    val authState by authViewModel.authState.collectAsState()
    val selectServerViewModel: SelectServerViewModel = viewModel(
        factory = SetupViewModelFactory(appConfigStore)
    )

    var isSetupComplete by remember { mutableStateOf(false) }

    when (authState) {
        is AuthState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is AuthState.Unauthenticated, is AuthState.Error -> {
            LaunchedEffect(authState) {
                if (authState is AuthState.Unauthenticated) {
                    isSetupComplete = false
                    appConfigStore.clearData()
                }
            }

            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { /* handled via authViewModel */ }
            )
        }

        is AuthState.Authenticated -> {
            if (!isSetupComplete) {
                LaunchedEffect(Unit) {
                    selectServerViewModel.checkSetupRequirements()
                }

                SelectServerScreen(
                    selectServerViewModel = selectServerViewModel,
                    onSetupComplete = { isSetupComplete = true }
                )
            } else {
                when (platform) {
                    Platform.Mobile -> MobileMainScaffold(authViewModel, appConfigStore)
                    Platform.TV -> TVMainScreen(authViewModel, appConfigStore)
                }
            }
        }

        AuthState.AwaitingLogin -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator()
                    Text("Signing you in...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
