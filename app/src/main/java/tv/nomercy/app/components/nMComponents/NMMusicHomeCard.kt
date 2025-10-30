package tv.nomercy.app.components.nMComponents

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.focusRequester
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMMusicCardWrapper
import tv.nomercy.app.shared.models.NMMusicHomeCardProps
import tv.nomercy.app.shared.models.NMMusicHomeCardWrapper
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.LocalCurrentItemFocusRequester
import tv.nomercy.app.shared.ui.LocalFocusLeftInRow
import tv.nomercy.app.shared.ui.LocalFocusRightInRow
import tv.nomercy.app.shared.ui.LocalOnActiveCardChange2
import tv.nomercy.app.shared.ui.LocalOnActiveInRow
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.isTv
import tv.nomercy.app.shared.utils.pickPaletteColor
import android.view.KeyEvent as AndroidKeyEvent

@Composable
fun NMMusicHomeCard(
    component: Component,
    modifier: Modifier,
    navController: NavHostController,
    aspectRatio: AspectRatio? = null,
) {
    val wrapper = component.props as? NMMusicHomeCardWrapper ?: return
    val data = wrapper.data

    val context = LocalContext.current

    val serverConfigStore = GlobalStores.getServerConfigStore(context)
    serverConfigStore.currentServer.collectAsState()

    val systemAppConfigStore = GlobalStores.getAppConfigStore(context)
    val useAutoThemeColors by systemAppConfigStore.useAutoThemeColors.collectAsState()

    val fallbackColor = MaterialTheme.colorScheme.primary
    val focusColor = remember(data.colorPalette?.cover) {
        if (!useAutoThemeColors) fallbackColor
        else pickPaletteColor(data.colorPalette?.cover) ?:fallbackColor
    }

    val backgroundGradient = if (data.type != "playlists") {
        Brush.verticalGradient(
            colors = listOf(
                focusColor.copy(alpha = 0.08f),
                focusColor.copy(alpha = 0.03f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                focusColor.copy(alpha = 0.20f),
                focusColor.copy(alpha = 0.15f),
                focusColor.copy(alpha = 0.10f),
                focusColor.copy(alpha = 0.05f),
            )
        )
    }

    val labelText = "Most listened ${data.type?.removeSuffix("s")?.replaceFirstChar { it.uppercase() }}"

    // TV focus/hover and key handling setup
    val interaction = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }
    LaunchedEffect(interaction) {
        interaction.interactions.collect { inter ->
            when (inter) {
                is FocusInteraction.Focus -> isFocused = true
                is FocusInteraction.Unfocus -> isFocused = false
                is HoverInteraction.Enter -> isHovered = true
                is HoverInteraction.Exit -> isHovered = false
            }
        }
    }
    val isTvPlatform = isTv()
    val isActive = isTvPlatform && (isFocused || isHovered)
    val borderWidth = animateDpAsState(
        targetValue = if (isActive) 2.dp else 1.dp,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "nmmusichomecard-border"
    ).value
    val scale = animateFloatAsState(
        targetValue = if (isActive) 1.015f else 1.0f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "nmmusichomecard-scale"
    ).value

    // Notify screen and parent row when this item becomes active
//    val onActiveCardChange2 = LocalOnActiveCardChange2.current
//    val onActiveInRow = LocalOnActiveInRow.current
//    LaunchedEffect(isActive) {
//        if (isActive) {
//            onActiveCardChange2(data)
//            onActiveInRow()
//        }
//    }

    val itemFocusRequester = LocalCurrentItemFocusRequester.current
    val focusLeftInRow = LocalFocusLeftInRow.current
    val focusRightInRow = LocalFocusRightInRow.current
    val keyScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = backgroundGradient
            )
            .graphicsLayer { if (isTvPlatform) { scaleX = scale; scaleY = scale } }
            .then(if (itemFocusRequester != null) Modifier.focusRequester(itemFocusRequester) else Modifier)
            .then(
                if (isTvPlatform) Modifier
                    .focusable(interactionSource = interaction)
                    .hoverable(interactionSource = interaction)
                    .semantics { role = Role.Button }
                else if (useAutoThemeColors) Modifier
                    .border(borderWidth, focusColor, RoundedCornerShape(20.dp))
                else Modifier
            )
            .onPreviewKeyEvent { event ->
                if (event.nativeKeyEvent.action == AndroidKeyEvent.ACTION_DOWN) {
                    when (event.nativeKeyEvent.keyCode) {
                        AndroidKeyEvent.KEYCODE_DPAD_LEFT -> { keyScope.launch { focusLeftInRow() }; true }
                        AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> { keyScope.launch { focusRightInRow() }; true }
                        AndroidKeyEvent.KEYCODE_DPAD_CENTER -> { keyScope.launch { data.link?.let { navController.navigate(it) } }; true }
                        else -> false
                    }
                } else false
            }
            .clickable { data.link.let { navController.navigate(it) } }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image container
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFDfeffe).copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                MusicHomeCardImage(
                    data = data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectFromType(AspectRatio.Cover)
                )
            }

            // Text content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = data.name.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}