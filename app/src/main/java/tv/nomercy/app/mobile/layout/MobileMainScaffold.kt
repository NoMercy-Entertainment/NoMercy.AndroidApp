import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import tv.nomercy.app.mobile.layout.BottomNavigationBar
import tv.nomercy.app.shared.routes.AppNavItem
import tv.nomercy.app.shared.routes.MobileNavHost

@Composable
fun MobileMainScaffold(
) {
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
                navItems = navItems.filter { it.isMobileVisible },
            )
        }
    ) { innerPadding ->
        MobileNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}