import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import tv.nomercy.app.mobile.screens.auth.AuthViewModel
import tv.nomercy.app.mobile.screens.base.library.LibraryScreen
import tv.nomercy.app.shared.stores.AppConfigStore
import tv.nomercy.app.mobile.screens.dashboard.profile.AboutScreen
import tv.nomercy.app.mobile.screens.base.MobileHomeScreen
import tv.nomercy.app.mobile.screens.music.MusicScreen
import tv.nomercy.app.mobile.screens.dashboard.profile.ProfileScreen
import tv.nomercy.app.mobile.screens.base.SearchScreen
import tv.nomercy.app.mobile.screens.dashboard.profile.ServerInfoScreen
import tv.nomercy.app.mobile.screens.selectServer.ServerSelectionScreen

@Composable
fun MobileNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    appConfigStore: AppConfigStore,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = AppNavItem.Home.route, modifier = modifier) {
        composable(AppNavItem.Home.route) { MobileHomeScreen() }
        composable(AppNavItem.Libraries.route) { LibraryScreen(navController) }
        composable(AppNavItem.Search.route) { SearchScreen() }
        composable(AppNavItem.Music.route) { MusicScreen() }
        composable(AppNavItem.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                appConfigStore = appConfigStore,
                onNavigateToServerSelection = { navController.navigate(AppNavItem.ServerSelection.route) },
                onNavigateToServerInfo = { navController.navigate(AppNavItem.ServerInfo.route) },
                onNavigateToAbout = { navController.navigate(AppNavItem.About.route) }
            )
        }
        composable(AppNavItem.ServerSelection.route) {
            ServerSelectionScreen(appConfigStore, onNavigateBack = { navController.popBackStack() })
        }
        composable(AppNavItem.ServerInfo.route) {
            ServerInfoScreen(appConfigStore, onNavigateBack = { navController.popBackStack() })
        }
        composable(AppNavItem.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}