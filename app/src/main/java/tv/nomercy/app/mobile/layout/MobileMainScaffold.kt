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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import tv.nomercy.app.R
import tv.nomercy.app.mobile.layout.BottomNavigationBar
import tv.nomercy.app.shared.components.music.FullPlayerScreen
import tv.nomercy.app.shared.components.music.MiniPlayer
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

    var isMiniPlayerVisible by remember { mutableStateOf(true) }
    var isFullPlayerOpen by remember { mutableStateOf(false) }

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
                        onOpenFullPlayer = { isFullPlayerOpen = true },
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
                visible = !isImmersive && isMiniPlayerVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                FullPlayerScreen(
                    isOpen = isFullPlayerOpen,
                    onDismiss = { isFullPlayerOpen = false }
                )
            }
        }
    }
}

annotation class AppNavItem(
    val route: String,
    val title: Int,
    val icon: Int,
    val description: String
)
