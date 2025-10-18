package tv.nomercy.app.tv.layout

import AppNavItem
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import tv.nomercy.app.R
import tv.nomercy.app.shared.components.MoooomIconName
import tv.nomercy.app.shared.routes.TvNavHost
import tv.nomercy.app.shared.stores.GlobalStores

@Composable
fun TVMainScaffold() {
    GlobalStores.getServerConfigStore(LocalContext.current)
    GlobalStores.getLibraryStore(LocalContext.current)
    GlobalStores.getAppConfigStore(LocalContext.current)

    val navController = rememberNavController()
    val navItems = listOf(
        AppNavItem("/home", R.string.title_home, MoooomIconName.Home1),
        AppNavItem("/libraries", R.string.title_libraries, MoooomIconName.Folder),
        AppNavItem("/music/start", R.string.title_music, MoooomIconName.NoteEighthPair),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            TvNavHost(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
            )

            TvNavigationBar(
                navController = navController,
                navItems = navItems,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .zIndex(1f)
            )
        }
    }
}