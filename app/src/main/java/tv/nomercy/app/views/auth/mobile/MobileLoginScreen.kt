package tv.nomercy.app.views.auth.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.nomercy.app.views.base.auth.shared.AuthViewModel
import tv.nomercy.app.views.base.auth.shared.AuthState
import tv.nomercy.app.views.base.auth.mobile.MobileLoginContent

@Composable
fun MobileLoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    MobileLoginContent(authState = authState, authViewModel = authViewModel)
}
