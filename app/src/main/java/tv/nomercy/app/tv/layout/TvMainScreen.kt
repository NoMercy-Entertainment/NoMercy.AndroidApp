package tv.nomercy.app.tv.layout

import tv.nomercy.app.shared.routes.AppNavItem
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import tv.nomercy.app.mobile.layout.BottomNavigationBar
import tv.nomercy.app.shared.routes.MobileNavHost
import tv.nomercy.app.shared.stores.GlobalStores


@Composable
fun TVMainScaffold() {
    GlobalStores.getServerConfigStore(LocalContext.current)
    GlobalStores.getLibraryStore(LocalContext.current)
    GlobalStores.getAppConfigStore(LocalContext.current)

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