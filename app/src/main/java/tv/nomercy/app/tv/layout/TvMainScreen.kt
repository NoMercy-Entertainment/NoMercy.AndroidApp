package tv.nomercy.app.tv.layout

import AppNavItem
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import tv.nomercy.app.R
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