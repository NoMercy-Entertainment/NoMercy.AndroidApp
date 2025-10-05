package tv.nomercy.app.tv.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.nomercy.app.mobile.screens.auth.AuthState
import tv.nomercy.app.mobile.screens.auth.AuthViewModel

@Composable
fun TvLoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    authState: AuthState = authViewModel.authState.value
) {
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    when (authState) {
        is AuthState.TvInstructions -> {
            TvLoginInstructions(
                verificationUri = authState.verificationUri
                    .replace("auth-", "")
                    .replace("/realms/NoMercyTV/device", "/tv"),
                userCode = authState.userCode,
                onConfirm = { authViewModel.pollForToken() }
            )
        }
        is AuthState.TvPolling -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Waiting for login on another device...")
            }
        }
        is AuthState.Error -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(authState.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = { authViewModel.clearError() }) {
                    Text("Try Again")
                }
            }
        }
        else -> {
            Button(onClick = { authViewModel.login() }) {
                Text("Start Login")
            }
        }
    }
}