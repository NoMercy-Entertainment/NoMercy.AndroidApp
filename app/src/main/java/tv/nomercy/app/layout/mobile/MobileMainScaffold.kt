package tv.nomercy.app.layout.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import tv.nomercy.app.R
import tv.nomercy.app.layout.mobile.BottomNavigationBar
import tv.nomercy.app.components.MoooomIconName
import tv.nomercy.app.components.music.FullPlayerScreen
import tv.nomercy.app.components.music.MiniPlayer
import tv.nomercy.app.shared.routes.MobileNavHost
import tv.nomercy.app.shared.stores.GlobalStores

@Composable
fun MobileMainScaffold(
    isImmersive: Boolean = false
) {
    val navController = rememberNavController()
    val navItems = listOf(
        AppNavItem("/home", R.string.title_home, MoooomIconName.Home1),
        AppNavItem("/search", R.string.title_search, MoooomIconName.SearchMagnifyingGlass),
        AppNavItem("/libraries", R.string.title_libraries, MoooomIconName.Folder),
        AppNavItem("/music/start", R.string.title_music, MoooomIconName.NoteEighthPair),
        AppNavItem("/profile", R.string.title_profile, MoooomIconName.User),
    )

    var isMiniPlayerVisible by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val musicPlayerStore = GlobalStores.getMusicPlayerStore(context)
    val isFullPlayerOpen by musicPlayerStore.isFullPlayerOpen.collectAsState()

    Scaffold(
        bottomBar = {
            Column(Modifier.zIndex(0f)) {
                AnimatedVisibility(
                    visible = !isImmersive && isMiniPlayerVisible,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    MiniPlayer(
                        navController = navController,
                        isOpen = isMiniPlayerVisible,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                        onStopPlayback = { isMiniPlayerVisible = false }
                    )
                }

                AnimatedVisibility(
                    visible = !isImmersive,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    BottomNavigationBar(
                        navController = navController,
                        navItems = navItems,
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
            MobileNavHost(
                navController = navController,
                modifier = Modifier.fillMaxSize()
            )

            AnimatedVisibility(
                visible = isFullPlayerOpen && isMiniPlayerVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                FullPlayerScreen()
            }
        }
    }
}

class AppNavItem(
    val route: String,
    val name: Int,
    val icon: MoooomIconName,
)
