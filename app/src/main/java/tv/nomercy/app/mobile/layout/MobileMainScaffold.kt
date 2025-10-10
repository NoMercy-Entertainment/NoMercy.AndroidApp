import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import tv.nomercy.app.R
import tv.nomercy.app.mobile.layout.BottomNavigationBar
import tv.nomercy.app.shared.routes.MobileNavHost

@Composable
fun MobileMainScaffold(
    isImmersive: Boolean = false
) {
    val navController = rememberNavController()
    val navItems = listOf(
        AppNavItem("/home", R.string.title_home, R.drawable.home1, "Main dashboard"),
        AppNavItem("/search", R.string.title_search, R.drawable.searchmagnifyingglass, "Global search"),
        AppNavItem("/libraries", R.string.title_libraries, R.drawable.folder, "Libraries"),
        AppNavItem("/music/start", R.string.title_music, R.drawable.noteeighthpair, "Music playback"),
        AppNavItem("/profile", R.string.title_profile, R.drawable.user, "User settings"),
    )

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = !isImmersive,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(150))
            ) {
                BottomNavigationBar(
                    navController = navController,
                    navItems = navItems,
                )
            }
        }
    ) {
        innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                MobileNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
    }
}

annotation class AppNavItem(
    val route: String,
    val title: Int,
    val icon: Int,
    val description: String
)
