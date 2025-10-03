package tv.nomercy.app.ui.phone

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.nomercy.app.store.GlobalStores
import tv.nomercy.app.ui.components.EmptyGrid
import tv.nomercy.app.ui.components.LibraryTabScroller
import tv.nomercy.app.ui.components.NMCarousel
import tv.nomercy.app.ui.components.NMGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen() {
    val appConfigStore = GlobalStores.getAppConfigStore(LocalContext.current)
    val libraryStore = GlobalStores.getLibraryStore(LocalContext.current)

    val viewModel: LibrariesViewModel = viewModel(
        factory = LibrariesViewModelFactory(
            libraryStore = libraryStore,
            appConfigStore = appConfigStore
        )
    )

    // Collect state from ViewModel
    val libraries by viewModel.libraries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentLibraryId by viewModel.currentLibraryId.collectAsState()
    val currentLibrary by viewModel.currentLibrary.collectAsState()

    LaunchedEffect(libraries) {
        if (libraries.isNotEmpty() && currentLibrary.isEmpty()) {
            viewModel.loadLibrary(libraries.first().link)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Error handling
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
                viewModel.setCurrentLibraryId(tab.link)
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

                !isLoading && libraries.isEmpty() -> {
                    EmptyGrid(modifier = Modifier, text = "No libraries available.")
                }
            }

            LibraryContent(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun LibraryContent(
    viewModel: LibrariesViewModel,
    modifier: Modifier,
) {
    val currentLibrary by viewModel.currentLibrary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (!isLoading == currentLibrary.isEmpty()) {
            EmptyGrid(modifier = modifier, text = "No content available in this library.")
    }

    if(currentLibrary.isNotEmpty()) {
        currentLibrary.forEach { component ->
            when (component.component) {
                "NMGrid" -> {
                    NMGrid(
                        gridItems = component.props.items,
                        modifier = modifier
                    )
                }

                "NMCarousel" -> {
                    NMCarousel(
                        title = component.props.title,
                        items = component.props.items,
                        modifier = modifier.padding(vertical = 8.dp)
                    )
                }

                else -> {
                    Text(
                        text = "Unsupported component type: ${component.component}",
                        modifier = modifier.padding(16.dp)
                    )
                }
            }
        }
    }

}