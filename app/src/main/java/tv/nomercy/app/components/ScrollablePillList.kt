package tv.nomercy.app.components

import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class TabItem(
    val id: String,
    val title: String,
    val type: String,
    val link: String,
    val icon: Int,
    val onClick: () -> Unit
)

@Composable
fun ScrollablePillList(tabs: List<TabItem>, selectedTab: Int) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedTab) {
        val layoutInfo = listState.layoutInfo
        val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedTab }

        if (itemInfo != null) {
            val itemCenter = itemInfo.offset + itemInfo.size / 2
            val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
            val delta = itemCenter - viewportCenter

            listState.animateScrollBy(value = delta.toFloat(), animationSpec = spring())
        } else {
            listState.animateScrollToItem(index = selectedTab)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(tabs) { index, tab ->
            PillTab(
                title = tab.title,
                icon = tab.icon,
                isSelected = selectedTab == index,
                onClick = tab.onClick
            )
        }
    }
}