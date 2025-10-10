package tv.nomercy.app.mobile.screens.base.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.components.EmptyGrid
import tv.nomercy.app.shared.components.Indexer
import tv.nomercy.app.shared.components.LibraryTabScroller
import tv.nomercy.app.shared.components.nMComponents.NMComponent

@Composable
fun LibraryScreen(navController: NavController, libraryId: Any?, letter: Any? = null) {
    val viewModel: LibrariesViewModel = viewModel(
        factory = LibrariesViewModelFactory(
            libraryStore = GlobalStores.getLibraryStore(LocalContext.current),
        )
    )

    val libraries by viewModel.libraries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentLibrary by viewModel.currentLibrary.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()
    val isEmptyStable by viewModel.isEmptyStable.collectAsState()

    LaunchedEffect(libraries, libraryId) {
        if (libraries.isNotEmpty()) {
            if (libraryId == null) {
                viewModel.selectLibrary(libraries.first().link)
            } else {
                viewModel.selectLibrary(libraryId.toString())
            }
        }
    }

    val lazyGridState = rememberLazyGridState()

    val firstVisibleItemIndex by remember {
        derivedStateOf { lazyGridState.firstVisibleItemIndex }
    }

    val visibleChar = remember(firstVisibleItemIndex, currentLibrary) {
        val gridItems = currentLibrary.firstOrNull { it.component == "NMGrid" }?.props?.items
        val item = gridItems?.getOrNull(firstVisibleItemIndex)
        val titleSort = item?.props?.data?.titleSort.orEmpty()
        val char = titleSort.firstOrNull()?.uppercaseChar() ?: letter
        if (char in 'A'..'Z') char else '#'
    }

    LaunchedEffect(visibleChar) {
        val index = viewModel.indexerCharacters.indexOf(visibleChar)
        if (index != -1 && index != selectedIndex) {
            viewModel.setSelectedIndexFromScroll(index)
        }
    }

    LaunchedEffect(viewModel, currentLibrary) {
        viewModel.scrollRequest.collectLatest {
            if (it != null && currentLibrary.isNotEmpty()) {
                lazyGridState.scrollToItem(it)
                viewModel.onScrollRequestCompleted()
            }
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

        LibraryTabScroller(viewModel, currentLink = libraryId?.toString(), navController = navController)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        isEmptyStable -> {
                            EmptyGrid(modifier = Modifier.fillMaxSize(), text = "No content available in this library.")
                        }
                        else -> {
                            NMComponent(
                                components = currentLibrary,
                                navController = navController,
                                modifier = Modifier.fillMaxSize(),
                                lazyGridState = lazyGridState
                            )
                        }
                    }
                }

                Indexer(
                    modifier = Modifier,
                    viewModel = viewModel
                )
            }
        }
    }
}
