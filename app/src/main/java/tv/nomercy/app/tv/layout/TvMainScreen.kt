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
        AppNavItem("/home", R.string.title_home, R.drawable.home1, "Main dashboard"),
        AppNavItem("/search", R.string.title_search, R.drawable.searchmagnifyingglass, "Global search"),
        AppNavItem("/libraries", R.string.title_libraries, R.drawable.folder, "Libraries"),
        AppNavItem("/music/start", R.string.title_music, R.drawable.noteeighthpair, "Music playback"),
        AppNavItem("/profile", R.string.title_profile, R.drawable.user, "User settings"),
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