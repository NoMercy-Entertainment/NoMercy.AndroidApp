package tv.nomercy.app.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.ui.RowFocusController


/**
 * Centralizes vertical focus routing and alignment for the Home TV LazyColumn.
 *
 * Responsibilities:
 * - Maintain a registry of row focus controllers.
 * - Move focus to specific items/rows on DPAD navigation.
 * - Ensure target rows are visible and aligned before requesting item focus.
 */
class ColumnFocusRouter(
    private val listState: LazyListState,
    private val rowControllers: MutableMap<Int, RowFocusController>,
    private val data: List<Component>,
    private val topOffsetPx: Int = 0
) {
    /**
     * Register or replace the RowFocusController for a given row index.
     */
    fun registerController(index: Int, controller: RowFocusController) {
        rowControllers[index] = controller
    }

    /**
     * Focus a specific item in a specific row. Ensures the row is brought into view first.
     */
    suspend fun focusAt(rowIndex: Int, itemIndex: Int) {
        if (rowIndex in data.indices) {
            listState.animateScrollToItem(rowIndex, topOffsetPx)
            rowControllers[rowIndex]?.focusItem?.invoke(itemIndex)
        }
    }

    /**
     * Focus the item in the target row whose start edge is closest to the given viewport X.
     * Useful for aligning vertical navigation with what is visually above/below.
     */
    suspend fun focusAtViewportX(rowIndex: Int, viewportX: Float) {
        if (rowIndex in data.indices) {
            listState.animateScrollToItem(rowIndex, topOffsetPx)
            rowControllers[rowIndex]?.focusNearestAtViewportX?.invoke(viewportX)
        }
    }

    /**
     * Move focus to the left-most fully visible item of the given row.
     * Waits for the row to be visible and laid out before delegating to the row controller.
     */
    suspend fun focusFirstVisible(rowIndex: Int) {
        if (rowIndex in data.indices) {
            listState.animateScrollToItem(rowIndex, topOffsetPx)
            var attempts = 0
            while (!listState.layoutInfo.visibleItemsInfo.any { it.index == rowIndex } && attempts < 60) {
                delay(16)
                attempts++
            }
            withFrameNanos { }
            rowControllers[rowIndex]?.focusFirstVisible?.invoke()
        }
    }

    /**
     * Handoff from the navbar into the screen: focus the first focusable content element.
     * Scrolls to the first row, waits for controller registration and a frame, then focuses its first visible item.
     */
    suspend fun focusFirstInContent() {
        if (data.isNotEmpty()) {
            listState.animateScrollToItem(0, topOffsetPx)
            var attempts = 0
            while (rowControllers[0] == null && attempts < 60) {
                delay(16)
                attempts++
            }
            withFrameNanos { }
            rowControllers[0]?.focusFirstVisible?.invoke()
        }
    }

    /**
     * Called once when data arrives to ensure the first card is focused.
     * Scrolls to row 0, waits for controller registration and a frame, then focuses item 0.
     */
    suspend fun requestInitialFocus() {
        if (data.isNotEmpty()) {
            listState.animateScrollToItem(0, topOffsetPx)
            var attempts = 0
            while (rowControllers[0] == null && attempts < 60) {
                delay(16)
                attempts++
            }
            withFrameNanos { }
            rowControllers[0]?.focusItem?.invoke(0)
        }
    }

    /**
     * Align the given row to the top of the viewport without animation.
     */
    suspend fun alignRow(rowIndex: Int) {
        listState.scrollToItem(rowIndex, topOffsetPx)
    }
}
