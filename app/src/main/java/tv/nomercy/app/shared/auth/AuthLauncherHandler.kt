package tv.nomercy.app.shared.auth
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import tv.nomercy.app.views.base.auth.shared.AuthViewModel

@Composable
fun HandleAuthResponse(
    authViewModel: AuthViewModel,
    launchAuthIntent: (Intent) -> Unit
) {
    val loginIntent by authViewModel.loginIntent.collectAsState()

    LaunchedEffect(loginIntent) {
        loginIntent?.let { intent ->
            launchAuthIntent(intent.intent)
            authViewModel.clearLoginIntent()
        }
    }
}