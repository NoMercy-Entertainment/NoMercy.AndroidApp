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
) {
    val navController = rememberNavController()
    val navItems = listOf(
        AppNavItem("/home", "Home", R.drawable.home1, "Main dashboard"),
        AppNavItem("/libraries", "Libraries", R.drawable.folder, "Libraries"),
        AppNavItem("/search", "Search", R.drawable.searchmagnifyingglass, "Global search"),
        AppNavItem("/music/start", "Music", R.drawable.noteeighthpair, "Music playback"),
        AppNavItem("/profile", "Profile", R.drawable.user, "User settings"),
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                navItems = navItems,
            )
        }
    ) { innerPadding ->
        MobileNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

annotation class AppNavItem(
    val route: String,
    val title: String,
    val icon: Int,
    val description: String
)
