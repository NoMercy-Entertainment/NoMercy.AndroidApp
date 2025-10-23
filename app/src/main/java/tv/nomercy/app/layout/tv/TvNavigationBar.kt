package tv.nomercy.app.layout.tv

import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import tv.nomercy.app.layout.mobile.AppNavItem
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.CoverImage
import tv.nomercy.app.components.Marquee
import tv.nomercy.app.components.MoooomIconName
import tv.nomercy.app.shared.models.PlaylistItem
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.components.ProfileImage
import tv.nomercy.app.components.brand.AppLogoSquare
import tv.nomercy.app.components.music.EqSpinner
import tv.nomercy.app.components.music.TrackLinksArtists
import tv.nomercy.app.components.music.TvMiniPlayer
import tv.nomercy.app.shared.ui.LocalNavbarFocusBridge

@Composable
fun TvNavigationBar(
    navController: NavHostController,
    navItems: List<AppNavItem>,
    modifier: Modifier = Modifier
) {

    val musicStore = GlobalStores.getMusicPlayerStore(LocalContext.current)
    val currentSong by musicStore.currentSong.collectAsState()
    val isPlaying by musicStore.isPlaying.collectAsState()

    Row(
        modifier = modifier
            .background(Color.Transparent)
            .padding(horizontal = 36.dp)
            .fillMaxWidth()
            .height(70.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .height(52.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentSong != null) {
                TvMiniPlayer(
                    song = currentSong!!,
                    isPlaying = isPlaying,
                    navController = navController,
                )
            } else {
                AppLogoSquare()
            }
        }

        Row(
            modifier = Modifier
                .weight(2f)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                navItems.forEach { item ->
                    TvNavigationBarButton(item, navController)
                }
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileImage(modifier = Modifier.size(40.dp))
        }
    }
}



@Composable
fun TvNavigationBarButton(
    item: AppNavItem,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

    val inactiveBg = Color.Black.copy(alpha = 0.6f)
    val activeBrush = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)))

    // Add hover/focus state distinct from active selection
    val interaction = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    var isFocused by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var isHovered by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(interaction) {
        interaction.interactions.collect { inter ->
            when (inter) {
                is androidx.compose.foundation.interaction.FocusInteraction.Focus -> isFocused = true
                is androidx.compose.foundation.interaction.FocusInteraction.Unfocus -> isFocused = false
                is androidx.compose.foundation.interaction.HoverInteraction.Enter -> isHovered = true
                is androidx.compose.foundation.interaction.HoverInteraction.Exit -> isHovered = false
            }
        }
    }
    val outlineActive = (isFocused || isHovered)
    val outlineAlpha = androidx.compose.animation.core.animateFloatAsState(targetValue = if (outlineActive) 1f else 0f, label = "tvnav-outline-alpha").value
    val outlineWidth = 2.dp
    val outlineGap = 6.dp

    // Outer container reserves space for the outline so the button never moves
    val scope = rememberCoroutineScope()
    val navbarBridge = LocalNavbarFocusBridge.current
    Box(
        modifier = modifier
            .border(
                width = outlineWidth,
                color = MaterialTheme.colorScheme.primary.copy(alpha = outlineAlpha),
                shape = RoundedCornerShape(24.dp) // slightly larger than inner 20.dp to keep a visible offset curve
            )
            .padding(outlineGap)
            .focusable(interactionSource = interaction)
            .hoverable(interactionSource = interaction)
            .onPreviewKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            // Delegate focus to the bridge to move to the main content area
                            scope.launch {
                                navbarBridge.focusFirstInContent()
                            }
                            navController.currentBackStackEntry?.destination?.route == "/home"
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            navController.navigate(item.route)
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(interactionSource = interaction, indication = null) {
//                navController.navigate(item.route)
                val startId = navController.graph.startDestinationId
                val route = item.route
                if (route.isNotBlank()) {
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(startId) { saveState = true }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner fixed-size button; background changes with selection but size/shape remains constant
        Box(
            modifier = Modifier
                .then(
                    if (isSelected) Modifier
                        .background(activeBrush, shape = RoundedCornerShape(20.dp))
                        .shadow(6.dp, shape = RoundedCornerShape(20.dp))
                    else Modifier
                        .background(inactiveBg, shape = RoundedCornerShape(20.dp))
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
            ) {
                MoooomIcon(
                    icon = item.icon,
                    contentDescription = stringResource(item.name),
                    modifier = Modifier
                        .size(20.dp),
                    tint = Color.White
                )

                Text(
                    text = stringResource(item.name),
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}

