package tv.nomercy.app.shared.components.nMComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.ComponentData

@Composable
fun <T: ComponentData> NMGrid(
    component: Component<out T>,
    modifier: Modifier,
    navController: NavController,
    lazyGridState: LazyGridState?
) {
    val columns = 2
    val spacing = 16.dp

    LazyVerticalGrid(
        state = lazyGridState ?: rememberLazyGridState(),
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(
            top =  spacing / 2,
            end =  spacing / 2,
            bottom =  spacing,
            start = spacing,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        itemsIndexed(component.props.items, key = { _, item -> item.id }) { index, item ->
            NMComponent(
                components = listOf(item),
                navController = navController,
            )
        }
    }
}