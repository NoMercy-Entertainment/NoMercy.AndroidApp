package tv.nomercy.app.layout.tv

import tv.nomercy.app.layout.mobile.AppNavItem
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.hoverable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.components.MoooomIcon
import tv.nomercy.app.components.nMComponents.CoverImage
import tv.nomercy.app.shared.models.PlaylistItem
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.components.ProfileImage
import tv.nomercy.app.shared.components.brand.AppLogoSquare
import tv.nomercy.app.shared.ui.LocalNavbarFocusBridge
import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

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
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        if (currentSong != null) {
            SongPreviewButton(
                song = currentSong!!,
                isPlaying = isPlaying,
                onClick = {

                }
            )
        } else {
            AppLogoSquare()
        }

        Row(
            modifier = Modifier
                .wrapContentWidth()
                .fillMaxWidth()
                .weight(3f)
                .padding(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                navItems.forEach { item ->
                    TvNavigationBarButton(item, navController)
                }
            }

        }
        ProfileImage()
    }
}

@Composable
fun SongPreviewButton(
    song: PlaylistItem,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(240.dp)
            .height(40.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.size(40.dp)) {

            CoverImage(cover = song.cover, name = song.name, modifier = Modifier.matchParentSize().graphicsLayer { alpha = 0.5f })

            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                if (isPlaying) Text("▶", color = Color.White) else Text("▷", color = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MarqueeText(song.name)
            Text(
                text = song.artistTrack.joinToString(", ") { it.name },
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                if (event.nativeKeyEvent.action == AndroidKeyEvent.ACTION_DOWN) {
                    when (event.nativeKeyEvent.keyCode) {
                        AndroidKeyEvent.KEYCODE_DPAD_DOWN -> {
                            // Delegate focus to the bridge to move to the main content area
                            scope.launch {
                                navbarBridge.focusFirstInContent()
                            }
                            true
                        }
                        AndroidKeyEvent.KEYCODE_DPAD_CENTER -> {
                            navController.navigate(item.route)
                            true
                        }
                        else -> false
                    }
                } else true
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



@Composable fun MarqueeText(text: String) { Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis) }
