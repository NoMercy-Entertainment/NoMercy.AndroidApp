import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import tv.nomercy.app.mobile.screens.auth.AuthViewModel
import tv.nomercy.app.mobile.layout.BottomNavigationBar
import tv.nomercy.app.shared.stores.AppConfigStore

@Composable
fun AuthenticatedMainScreen(authViewModel: AuthViewModel, appConfigStore: AppConfigStore) {
    val navController = rememberNavController()
    val navItems = listOf(
        AppNavItem.Home,
        AppNavItem.Libraries,
        AppNavItem.Search,
        AppNavItem.Music,
        AppNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                navItems = navItems,
                appConfigStore = appConfigStore,
                authViewModel = authViewModel
            )
        }
    ) { innerPadding ->
        MobileNavHost(
            navController = navController,
            authViewModel = authViewModel,
            appConfigStore = appConfigStore,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
