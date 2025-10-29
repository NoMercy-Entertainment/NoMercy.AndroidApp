package tv.nomercy.app.components.nMComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.nomercy.app.components.images.TMDBImage
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMCardWrapper
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.getColorFromPercent
import tv.nomercy.app.shared.utils.isTv
import tv.nomercy.app.shared.utils.paletteBackground
import tv.nomercy.app.shared.utils.pickPaletteColor
import tv.nomercy.app.shared.ui.LocalOnActiveCardChange
import tv.nomercy.app.shared.ui.LocalCurrentItemFocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import android.view.KeyEvent
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.ui.LocalFocusLeftInRow
import tv.nomercy.app.shared.ui.LocalFocusRightInRow
import tv.nomercy.app.shared.ui.LocalOnActiveInRow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavHostController
import tv.nomercy.app.shared.stores.GlobalStores

@Composable
fun NMCard(
    component: Component,
    modifier: Modifier,
    navController: NavHostController,
    aspectRatio: AspectRatio? = null,
) {
    val wrapper = component.props as? NMCardWrapper ?: return
    val data = wrapper.data ?: return

    val context = LocalContext.current
    val systemAppConfigStore = GlobalStores.getAppConfigStore(context)
    val useAutoThemeColors by systemAppConfigStore.useAutoThemeColors.collectAsState()

    val fallbackColor = MaterialTheme.colorScheme.primary
    val focusColor: Color = remember(data.colorPalette) {
        if (!useAutoThemeColors) fallbackColor
        else pickPaletteColor(data.colorPalette?.poster, fallbackColor = fallbackColor)
    }

    // TV: add animated hover/focus border growth (default 1.dp)
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
        label = "nmcard-border"
    ).value
    val scale = animateFloatAsState(
        targetValue = if (isActive) 1.015f else 1.0f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "nmcard-scale"
    ).value

    // Notify listeners (if provided upstream) when this card becomes active on TV
    val onActiveCardChange = LocalOnActiveCardChange.current
    val onActiveInRow = LocalOnActiveInRow.current
    LaunchedEffect(isActive) {
        if (isActive) {
            onActiveCardChange(data)
            // Ask parent carousel (if any) to align this item to the left edge
            onActiveInRow()
        }
    }

    val itemFocusRequester = LocalCurrentItemFocusRequester.current
    val focusLeftInRow = LocalFocusLeftInRow.current
    val focusRightInRow = LocalFocusRightInRow.current
    val keyScope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .fillMaxSize()
            .aspectFromType(aspectRatio)
            .graphicsLayer { if (isTvPlatform) { scaleX = scale; scaleY = scale } }
            .then(
                if (itemFocusRequester != null) Modifier
                    .focusRequester(itemFocusRequester)
                else Modifier
            )
            .then(
                if (isTvPlatform) Modifier
                    .border(borderWidth, focusColor.copy(alpha = if (isActive) 1f else 0.5f), RoundedCornerShape(6.dp))
                    .focusable(interactionSource = interaction)
                    .hoverable(interactionSource = interaction)
                    .semantics { role = Role.Button }
                else if (useAutoThemeColors) Modifier
                    .border(borderWidth, focusColor, RoundedCornerShape(6.dp))
                else Modifier
            )
            .onPreviewKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> { keyScope.launch { focusLeftInRow() }; true }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> { keyScope.launch { focusRightInRow() }; true }
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            keyScope.launch {
                                data.link.let { navController.navigate(it) }
                            }; true
                        }
                        else -> false
                    }
                } else false
            },
        shape = RoundedCornerShape(6.dp),
        onClick = {
            data.link.let { navController.navigate(it) }
        }
    ) {
        if(data.title.startsWith("More ")) {
            // empty more card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.dp.value.sp),
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            return@Card
        }
        Box(modifier = Modifier
            .fillMaxSize()
            .then(if (useAutoThemeColors) Modifier.paletteBackground(data.colorPalette?.poster) else Modifier)
        ) {
            TMDBImage(
                path = data.poster,
                title = data.title,
                aspectRatio = AspectRatio.Poster,
                size = 180,
            )

            CompletionOverlay(
                data = {
                    OverlayProps(
                        numberOfItems = data.numberOfItems,
                        haveItems = data.haveItems,
                        type = data.type
                    )
                }(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 0.dp, top = 16.dp)
            )

            if (!isTv()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                        .align(Alignment.BottomStart)
                ) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.dp.value.sp),
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        minLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Serializable
data class OverlayProps(
    @SerialName("number_of_items")
    val numberOfItems: Int? = 0,
    @SerialName("have_items")
    val haveItems: Int? = 0,
    val type: String = ""
)


@Composable
fun CompletionOverlay(
    data: OverlayProps,
    modifier: Modifier = Modifier
) {
    if (isTv()) {
        // Don't show overlay on TV for now
        return
    }

    val percent = calculateCompletionPercent(data.haveItems, data.numberOfItems)
    val color = getColorFromPercent(percent)
    val collapsed = shouldCollapsePill(data)

    val value = remember(data.haveItems, data.numberOfItems) {
        if (data.haveItems == null || data.numberOfItems == null || (data.haveItems == 0 && data.numberOfItems == 0)) null
        else "${data.haveItems} of ${data.numberOfItems}"
    }

    if (value != null) {
        Box(
            modifier = modifier
                .background(
                    color = color,
                    shape = RoundedCornerShape(
                        topEnd = 6.dp,
                        bottomEnd = 6.dp
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(
                        topEnd = 6.dp,
                        bottomEnd = 6.dp
                    )
                )
                .padding(
                    horizontal = if (collapsed) 0.dp else 6.dp,
                    vertical = 1.dp
                )
                .defaultMinSize(
                    minWidth = 8.dp,
                    minHeight = 16.dp
                )
        ) {
            if (!collapsed) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if(percent > 30 && percent < 80) Color.Black else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun calculateCompletionPercent(haveItems: Int?, numberOfItems: Int?): Int {
    if (haveItems == null || numberOfItems == null || numberOfItems == 0) return 0
    return ((haveItems.toFloat() / numberOfItems) * 100).toInt().coerceIn(0, 100)
}

fun shouldCollapsePill(data: OverlayProps): Boolean {
    return data.numberOfItems == 1 &&
            (data.haveItems == 0 || data.haveItems == 1)
}