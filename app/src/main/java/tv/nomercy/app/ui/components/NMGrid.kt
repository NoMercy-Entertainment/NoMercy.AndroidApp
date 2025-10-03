package tv.nomercy.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import tv.nomercy.app.api.models.Component
import tv.nomercy.app.store.GlobalStores
import tv.nomercy.app.ui.phone.LibrariesViewModel
import tv.nomercy.app.ui.phone.LibrariesViewModelFactory
import kotlin.math.ceil

@Composable
fun <T> NMGrid(
    modifier: Modifier,
    component: Component<T>,
    navController: NavController
) {
    val appConfigStore = GlobalStores.getAppConfigStore(LocalContext.current)
    val libraryStore = GlobalStores.getLibraryStore(LocalContext.current)

    val viewModel: LibrariesViewModel = viewModel(
        factory = LibrariesViewModelFactory(
            libraryStore = libraryStore,
            appConfigStore = appConfigStore
        )
    )

    val maxWidth = 400.dp
    val columns = 2
    val padding = 16.dp
    val spacing = 16.dp

    val lazyGridState = rememberLazyGridState()

    LaunchedEffect(viewModel) {
        viewModel.scrollRequest.collectLatest {
            if (it != null) {
                lazyGridState.animateScrollToItem(it)
                viewModel.onScrollRequestCompleted()
            }
        }
    }

    val rows = ceil(component.props.items.size.toFloat() / columns).toInt()

    val itemHeight = (maxWidth / columns) * 3 / 2 - padding

    val totalHeight = (itemHeight * rows) + (spacing * (rows - 1).coerceAtLeast(0))

    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Fixed(columns),
        modifier = modifier
            .height(totalHeight)
            .border(1.dp, MaterialTheme.colorScheme.outline),
        contentPadding = PaddingValues(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        itemsIndexed(component.props.items) { index, item ->
            NMComponent(
                components = listOf(item),
                navController = navController,
                modifier = Modifier
            )
        }
    }
}