package tv.nomercy.app.tv.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
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
                expiresAt = authState.expiresAt,
                onConfirm = { authViewModel.pollForToken() }
            )
        }
        is AuthState.TvPolling -> {
            // Keep showing the QR/instructions while polling; do not re-trigger polling here
            TvLoginInstructions(
                verificationUri = authState.verificationUri
                    .replace("auth-", "")
                    .replace("/realms/NoMercyTV/device", "/tv"),
                userCode = authState.userCode,
                expiresAt = authState.expiresAt,
                onConfirm = { /* polling already in progress; no-op */ }
            )
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