package tv.nomercy.app.components.nMComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMCardProps
import tv.nomercy.app.shared.models.NMCardWrapper
import tv.nomercy.app.shared.models.NMCarouselProps
import tv.nomercy.app.shared.ui.LocalCurrentItemFocusRequester
import tv.nomercy.app.shared.ui.LocalFocusLeftInRow
import tv.nomercy.app.shared.ui.LocalFocusRightInRow
import tv.nomercy.app.shared.ui.LocalOnActiveInRow
import tv.nomercy.app.shared.ui.LocalOnActiveRowInColumn
import tv.nomercy.app.shared.ui.LocalRegisterRowFocusController
import tv.nomercy.app.shared.ui.LocalRequestFocusAtFirstVisible
import tv.nomercy.app.shared.ui.LocalRowIndex
import tv.nomercy.app.shared.ui.RowFocusController
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.utils.isTv
import kotlin.math.round
import android.view.KeyEvent as AndroidKeyEvent

// --- Delegates & helpers for clearer DX ---
/**
 * Builds a RowFocusController for a single carousel row.
 * - Ensures programmatic focus also scrolls the row so the item is visible and start-aligned.
 * - Provides helpers to focus by viewport X and to pick the left-most fully visible item.
 */
private fun createRowFocusController(
    rowState: LazyListState,
    focusRequesters: List<FocusRequester>,
    itemsSize: Int,
    stepPx: Float,
    paddingStartPx: Float
): RowFocusController {
    return RowFocusController(
        focusItem = { idx ->
            rowState.animateScrollToItem(idx)
            focusRequesters.getOrNull(idx)?.requestFocus()
        },
        focusNearestAtViewportX = { viewportX ->
            val firstIndex = rowState.firstVisibleItemIndex
            val firstOffset = rowState.firstVisibleItemScrollOffset.toFloat()
            val scrollPx = firstIndex * stepPx + firstOffset
            val rawIndex = ((viewportX + scrollPx - paddingStartPx) / stepPx)
            val target = round(rawIndex).toInt().coerceIn(0, itemsSize - 1)
            rowState.animateScrollToItem(target)
            focusRequesters.getOrNull(target)?.requestFocus()
        },
        focusFirstVisible = {
            val layoutInfo = rowState.layoutInfo
            val visible = layoutInfo.visibleItemsInfo
            val candidate = visible.firstOrNull { it.offset >= 0 }
                ?: visible.minByOrNull { it.offset }
            val target = (candidate?.index ?: rowState.firstVisibleItemIndex)
                .coerceIn(0, itemsSize - 1)
            rowState.animateScrollToItem(target)
            focusRequesters.getOrNull(target)?.requestFocus()
        }
    )
}

/**
 * Watches the LazyRow scroll position and, when scrolling actually changes content and settles,
 * focuses the left-most fully visible item. This avoids overriding focus when no scroll happened.
 */
private suspend fun watchScrollAndSyncFocus(
    rowState: LazyListState,
    controller: RowFocusController
) {
    var lastIndex = rowState.firstVisibleItemIndex
    var lastOffset = rowState.firstVisibleItemScrollOffset
    var positionChanged = false
    while (true) {
        val currIndex = rowState.firstVisibleItemIndex
        val currOffset = rowState.firstVisibleItemScrollOffset
        if (currIndex != lastIndex || currOffset != lastOffset) {
            positionChanged = true
            lastIndex = currIndex
            lastOffset = currOffset
        }
        if (!rowState.isScrollInProgress && positionChanged) {
            positionChanged = false
            controller.focusFirstVisible()
        }
        delay(4)
    }
}

/**
 * Called when a card becomes active within the row. Aligns the row vertically in the column
 * (top snap) and horizontally scrolls the row so this item is start-aligned.
 */
private fun makeOnActiveInRow(
    index: Int,
    rowState: LazyListState,
    onActiveRowInColumn: suspend () -> Unit
): suspend () -> Unit = suspend {
    onActiveRowInColumn()
    rowState.animateScrollToItem(index)
}

/**
 * Produces a suspending action to move focus to the previous item if within bounds.
 */
private fun makeFocusLeft(index: Int, controller: RowFocusController): suspend () -> Unit = suspend {
    if (index > 0) controller.focusItem(index - 1)
}

/**
 * Produces a suspending action to move focus to the next item if within bounds.
 */
private fun makeFocusRight(index: Int, lastIndex: Int, controller: RowFocusController): suspend () -> Unit = suspend {
    if (index < lastIndex) controller.focusItem(index + 1)
}

/**
 * Handles DPAD keys for a single item in the row:
 * - RIGHT/LEFT: focus next/previous item only if movement is possible; otherwise let others handle it.
 * - DOWN/UP: move focus to the first fully visible card of the adjacent row via the column router.
 */
private fun handleItemKey(
    code: Int,
    index: Int,
    lastIndex: Int,
    rowIndex: Int,
    scope: CoroutineScope,
    controller: RowFocusController,
    requestFocusAtFirstVisible: suspend (Int) -> Unit
): Boolean {
    return when (code) {
        AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> {
            if (index < lastIndex) {
                scope.launch { controller.focusItem(index + 1) }
                true
            } else false
        }
        AndroidKeyEvent.KEYCODE_DPAD_LEFT -> {
            if (index > 0) {
                scope.launch { controller.focusItem(index - 1) }
                true
            } else false
        }
        AndroidKeyEvent.KEYCODE_DPAD_DOWN -> {
            scope.launch { requestFocusAtFirstVisible(rowIndex + 1) }
            // Consume to prevent default focus from interfering
            true
        }
        AndroidKeyEvent.KEYCODE_DPAD_UP -> {
            if (rowIndex > 0) {
                scope.launch { requestFocusAtFirstVisible(rowIndex - 1) }
                // Consume to prevent default focus from interfering
                true
            } else false
        }
        else -> false
    }
}

/**
 * Horizontal content carousel with TV-first focus and snapping behavior.
 *
 * Key behaviors:
 * - Header/title with optional See all link.
 * - Items laid out in a LazyRow with start-edge snap on fling and a configurable peek.
 * - When a card becomes active, the row start-aligns that card and the column aligns the row to top.
 * - Deterministic DPAD routing across/within rows via RowFocusController.
 */
@Composable
fun NMCarousel(
    component: Component,
    modifier: Modifier = Modifier,
    navController: NavController,
    visibleCards: Int = if (isTv()) 7 else 3,
    peekFraction: Float = if (isTv()) 0.7f else 0.25f,
) {
    val props = component.props as? NMCarouselProps ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Header row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTv()) 36.dp else 52.dp)
                .padding(
                    start = if (isTv()) 40.dp else 16.dp,
                    end = 16.dp,
                    top = if (isTv()) 4.dp else 12.dp,
                    bottom = 4.dp
                )
        ) {
            Text(
                text = props.title.orEmpty(),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(4f)
            )

            if (!isTv()) {
                props.moreLink?.let {
                    Box(
                        modifier = Modifier
                            .clickable { navController.navigate(it) }
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = props.moreLinkText ?: "See all",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val spacing = 8.dp
            val totalSpacing = spacing * (visibleCards - 1)
            val cardWidth = (maxWidth - totalSpacing) / (visibleCards + peekFraction)

            val rowState = rememberLazyListState()

            // Access vertical align callback from parent column (if provided)
            val onActiveRowInColumn = LocalOnActiveRowInColumn.current

            // Deterministic vertical focus routing
            val rowIndex = LocalRowIndex.current
            val registerController = LocalRegisterRowFocusController.current
            val requestFocusAtFirstVisible = LocalRequestFocusAtFirstVisible.current

            val items = if (isTv() && props.moreLink != null) {
                val component = Component(
                    id = "",
                    component = "NMCard",
                    props = NMCardWrapper(
                        id = "",
                        title = "",
                        data = NMCardProps(
                            id = "",
                            title = "More ${props.title.orEmpty().replace("Latest in ", "")}",
                            titleSort = "",
                            link = props.moreLink,
                            type = "tv",
                        ),
                    ),
                )

                props.items + component
            } else {
                props.items
            }

            // Per-item FocusRequesters so we can programmatically move focus
            val focusRequesters = remember(items.size) {
                List(items.size) { FocusRequester() }
            }

            val density = LocalDensity.current
            val stepPx = with(density) { (cardWidth + spacing).toPx() }
            val paddingStartPx = with(density) { (spacing * 2).toPx() }

            // Register this row's focus controller with the parent screen
            val controller = createRowFocusController(
                rowState = rowState,
                focusRequesters = focusRequesters,
                itemsSize = items.size,
                stepPx = stepPx,
                paddingStartPx = paddingStartPx
            )
            LaunchedEffect(registerController, rowIndex, focusRequesters) {
                if (rowIndex >= 0) registerController(rowIndex, controller)
            }

            // Synchronize focus only when real scroll position changes
            LaunchedEffect(rowState) {
                watchScrollAndSyncFocus(rowState, controller)
            }

            val scope = rememberCoroutineScope()

            val endPadding = spacing + cardWidth * peekFraction - 48.dp

            val focusCoordinator = viewModel<FocusCoordinatorViewModel>()

            LazyRow(
                state = rowState,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentPadding = PaddingValues(
                    start = if (isTv()) 40.dp else (spacing * 2),
                    end = if (endPadding < 0.dp) 18.dp else endPadding
                ),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                flingBehavior = rememberSnapFlingBehavior(
                    lazyListState = rowState
                ),
            ) {
                itemsIndexed(items, key = { index, item -> item.id }) { index, item ->
                    val aspectRatio = aspectFromComponent(item.component)
                    val onActiveInRow = makeOnActiveInRow(index, rowState, onActiveRowInColumn)
                    val focusLeft = makeFocusLeft(index, controller)
                    val focusRight = makeFocusRight(index, items.lastIndex, controller)
                    CompositionLocalProvider(
                        LocalOnActiveInRow provides onActiveInRow,
                        LocalCurrentItemFocusRequester provides focusRequesters[index],
                        LocalFocusLeftInRow provides focusLeft,
                        LocalFocusRightInRow provides focusRight
                    ) {
                        NMComponent(
                            components = listOf(item),
                            navController = navController,
                            aspectRatio = aspectRatio,
                            modifier = Modifier
                                .width(cardWidth)
                                .aspectFromType(aspectRatio)
                                .focusRequester(if (index == 0) focusCoordinator.leftmostCarouselFocusRequester else FocusRequester.Default)
                                .onPreviewKeyEvent { event ->
                                    if (event.type == KeyEventType.KeyDown) {
                                        handleItemKey(
                                            code = event.nativeKeyEvent.keyCode,
                                            index = index,
                                            lastIndex = items.lastIndex,
                                            rowIndex = rowIndex,
                                            scope = scope,
                                            controller = controller,
                                            requestFocusAtFirstVisible = requestFocusAtFirstVisible
                                        )
                                    } else false
                                }
                        )
                    }
                }
            }
        }
    }
}

class FocusCoordinatorViewModel : ViewModel() {
    val leftmostCarouselFocusRequester = FocusRequester()
    val leftmostButtonFocusRequester = FocusRequester()
}

/**
 * Maps component names to a default AspectRatio used for sizing within carousels.
 */
fun aspectFromComponent(componentName: String?): AspectRatio {
    return when (componentName) {
        "NMCard" -> AspectRatio.Poster
        "NMMusicCard" -> AspectRatio.Poster
        else -> AspectRatio.Profile
    }
}