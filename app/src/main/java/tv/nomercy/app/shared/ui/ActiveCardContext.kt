package tv.nomercy.app.shared.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.focus.FocusRequester
import tv.nomercy.app.shared.models.NMCardProps
import tv.nomercy.app.shared.models.NMCardWrapper
import tv.nomercy.app.shared.models.NMMusicCardProps
import tv.nomercy.app.shared.models.NMMusicHomeCardProps
import tv.nomercy.app.shared.models.NMMusicHomeCardWrapper

/**
 * Notifies listeners when a generic card becomes active on TV (focused/hovered).
 *
 * Default provider is a no-op so non-TV screens or contexts that don't care remain unaffected.
 */
val LocalOnActiveCardChange = staticCompositionLocalOf { { _: NMCardProps? -> } }
val LocalOnActiveCardChange2 = staticCompositionLocalOf { { _: NMMusicCardProps? -> } }

/**
 * Inside a horizontal carousel (LazyRow), request the row to align the currently active
 * item to the start (left) edge. Intended to be invoked by an active card.
 *
 * Default provider is a no-op.
 */
val LocalOnActiveInRow = staticCompositionLocalOf<suspend () -> Unit> { { } }

/**
 * In a vertical list (LazyColumn), request aligning the current row (carousel)
 * to the top when one of its children becomes active.
 *
 * Default provider is a no-op.
 */
val LocalOnActiveRowInColumn = staticCompositionLocalOf<suspend () -> Unit> { { } }

/**
 * Row-level focus controller used by the parent screen to route DPAD focus deterministically.
 *
 * - focusItem: Focus a specific item by index. Implementations should also ensure the row is scrolled
 *   so the item is visible and preferably start-aligned.
 * - focusNearestAtViewportX: Focus the item whose start edge is closest to the provided viewport X
 *   (in pixels). Useful for aligning vertical navigation to what is visually above/below.
 * - focusFirstVisible: Focus the left-most fully visible item in the row. Often used after scrolls/flings
 *   or when moving focus vertically between rows.
 */
data class RowFocusController(
    val focusItem: suspend (index: Int) -> Unit,
    val focusNearestAtViewportX: suspend (viewportX: Float) -> Unit = { _ -> },
    val focusFirstVisible: suspend () -> Unit = {}
)

/**
 * Provides the current row index to descendants within a carousel item hierarchy.
 */
val LocalRowIndex = staticCompositionLocalOf<Int> { -1 }

/**
 * Registration hook for NMCarousel to register its RowFocusController with the parent screen
 * using the row index.
 */
val LocalRegisterRowFocusController = staticCompositionLocalOf<(rowIndex: Int, controller: RowFocusController) -> Unit> { { _, _ -> } }

/**
 * Request focus at a specific row and item index. The parent screen should route this
 * to the corresponding RowFocusController for that row.
 */
val LocalRequestFocusAt = staticCompositionLocalOf<suspend (rowIndex: Int, itemIndex: Int) -> Unit> { { _, _ -> } }

/**
 * Request focus at a specific row using a viewport X anchor (in pixels) so the row can choose
 * the item whose start edge is closest to that X. Helpful to align vertical navigation with
 * what appears visually above/below the current item.
 */
val LocalRequestFocusAtViewportX = staticCompositionLocalOf<suspend (rowIndex: Int, viewportX: Float) -> Unit> { { _, _ -> } }

/**
 * Request focusing the first visible item in the target row (left-most fully visible card).
 */
val LocalRequestFocusAtFirstVisible = staticCompositionLocalOf<suspend (rowIndex: Int) -> Unit> { { _ -> } }

// Provides a FocusRequester for the current item (e.g., a Card) so parents can programmatically request focus on it.
val LocalCurrentItemFocusRequester = staticCompositionLocalOf<FocusRequester?> { null }

// Bridge to allow the top navigation bar (a sibling composable) to move focus into the current screen's first focusable element.
// Provided at the scaffold level so both NavHost content and the navbar can access the same instance.
data class NavbarFocusBridge(var focusFirstInContent: (suspend () -> Unit) = { })
val LocalNavbarFocusBridge = staticCompositionLocalOf { NavbarFocusBridge() }

// Horizontal navigation helpers within a carousel row, provided per item.
val LocalFocusLeftInRow = staticCompositionLocalOf<suspend () -> Unit> { { } }
val LocalFocusRightInRow = staticCompositionLocalOf<suspend () -> Unit> { { } }
