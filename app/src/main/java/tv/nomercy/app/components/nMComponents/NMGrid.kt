package tv.nomercy.app.components.nMComponents

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMGridWrapper
import tv.nomercy.app.shared.utils.assertBoundedWidth

@Composable
fun NMGrid(
    component: Component,
    modifier: Modifier,
    navController: NavHostController,
    lazyGridState: LazyGridState?
) {
    val props = component.props as? NMGridWrapper ?: return

    val columns = 2
    val spacing = 16.dp

    val scrollState = rememberScrollState()

    LaunchedEffect(props.id) {
        scrollState.scrollTo(0)
    }

    LazyVerticalGrid(
        state = lazyGridState ?: rememberLazyGridState(),
        columns = GridCells.Fixed(columns),
        modifier = modifier
            .fillMaxWidth()
            .scrollable(scrollState, orientation = Orientation.Vertical)
            .assertBoundedWidth(),
        contentPadding = PaddingValues(
            top =  spacing / 2,
            end =  spacing / 2,
            bottom =  spacing,
            start = spacing,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing),

    ) {
        itemsIndexed(props.items, key = { _, item -> item.id }) { index, item ->
            NMComponent(
                components = listOf(item),
                navController = navController,
            )
        }
    }
}