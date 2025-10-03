package tv.nomercy.app.ui.phone

import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import tv.nomercy.app.store.GlobalStores
import tv.nomercy.app.ui.components.EmptyGrid
import tv.nomercy.app.ui.components.Indexer
import tv.nomercy.app.ui.components.LibraryTabScroller
import tv.nomercy.app.ui.components.NMCarousel
import tv.nomercy.app.ui.components.NMCard
import tv.nomercy.app.ui.components.NMComponent
import tv.nomercy.app.ui.components.NMGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen(navController: NavController) {
    val appConfigStore = GlobalStores.getAppConfigStore(LocalContext.current)
    val libraryStore = GlobalStores.getLibraryStore(LocalContext.current)

    val viewModel: LibrariesViewModel = viewModel(
        factory = LibrariesViewModelFactory(
            libraryStore = libraryStore,
            appConfigStore = appConfigStore
        )
    )

    val libraries by viewModel.libraries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentLibraryId by viewModel.currentLibraryId.collectAsState()
    val currentLibrary by viewModel.currentLibrary.collectAsState()
    val showIndexer by viewModel.showIndexer.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()

    LaunchedEffect(libraries) {
        if (libraries.isNotEmpty() && currentLibraryId == null) {
            viewModel.selectLibrary(libraries.first().link)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        errorMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        viewModel.clearError()
                        viewModel.refresh()
                    }) {
                        Text("Retry")
                    }
                }
            }
        }

        LibraryTabScroller(viewModel) { tab ->
            if (tab != null && tab.link != currentLibraryId) {
                viewModel.selectLibrary(tab.link)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                !isLoading && currentLibrary.isEmpty() -> {
                    EmptyGrid(modifier = Modifier.fillMaxSize(), text = "No content available in this library.")
                }
                else -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        val currentLibrary by viewModel.currentLibrary.collectAsState()

                        NMComponent(
                            components = currentLibrary,
                            navController = navController,
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                                .fillMaxSize(),
                        )
                        Indexer(
                            isEnabled = showIndexer,
                            selectedIndex = selectedIndex,
                            onIndexSelected = { char -> viewModel.onIndexSelected(char) }
                        )
                    }
                }
            }
        }
    }
}
