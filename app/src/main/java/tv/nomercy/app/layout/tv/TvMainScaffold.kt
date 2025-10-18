package tv.nomercy.app.layout.tv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import tv.nomercy.app.layout.mobile.AppNavItem
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import tv.nomercy.app.R
import tv.nomercy.app.shared.components.MoooomIconName
import tv.nomercy.app.shared.components.music.FullPlayerScreen
import tv.nomercy.app.shared.routes.MobileNavHost
import tv.nomercy.app.shared.routes.TvNavHost
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.LocalNavbarFocusBridge
import tv.nomercy.app.shared.ui.NavbarFocusBridge

@Composable
fun TvMainScaffold(
    isImmersive: Boolean = false
) {
    GlobalStores.getServerConfigStore(LocalContext.current)
    GlobalStores.getLibraryStore(LocalContext.current)
    GlobalStores.getAppConfigStore(LocalContext.current)

    val navController = rememberNavController()
    val navItems = listOf(
        AppNavItem("/home", R.string.title_home, MoooomIconName.Home1),
        AppNavItem("/libraries", R.string.title_libraries, MoooomIconName.Folder),
        AppNavItem("/music/start", R.string.title_music, MoooomIconName.NoteEighthPair),
    )

    val navbarFocusBridge = remember { NavbarFocusBridge() }

//    var isFullPlayerOpen by remember { mutableStateOf(false) }
//
//    navController.addOnDestinationChangedListener { _, destination, _ ->
//        isFullPlayerOpen = destination.route?.contains("/watch") == true
//        isFullPlayerOpen = false
//    }


    CompositionLocalProvider(LocalNavbarFocusBridge provides navbarFocusBridge) {
        Scaffold(
            topBar = {
                Box(modifier = Modifier.fillMaxSize()) {
                    TvNavHost(
                        navController = navController,
                        modifier = Modifier
                            .fillMaxSize()
                    )

                    AnimatedVisibility(
                        visible = !isImmersive,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
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
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .padding(innerPadding)
            ) {
                TvNavHost(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
