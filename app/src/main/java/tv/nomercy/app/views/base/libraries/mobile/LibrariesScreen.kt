package tv.nomercy.app.views.base.libraries.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import tv.nomercy.app.R
import tv.nomercy.app.components.EmptyGrid
import tv.nomercy.app.components.Indexer
import tv.nomercy.app.components.nMComponents.NMComponent
import tv.nomercy.app.shared.models.NMCardWrapper
import tv.nomercy.app.shared.models.NMHomeCardWrapper
import tv.nomercy.app.shared.models.NMGridProps
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.utils.pickPaletteColor
import java.util.UUID
import tv.nomercy.app.views.base.libraries.shared.LibrariesViewModel
import tv.nomercy.app.views.base.libraries.shared.LibrariesViewModelFactory

@Composable
fun LibrariesScreen(navController: NavHostController) {

    val viewModel: LibrariesViewModel = viewModel(
        factory = LibrariesViewModelFactory(
            librariesStore = GlobalStores.getLibrariesStore(LocalContext.current),
        )
    )

    val librariesData by viewModel.librariesData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isEmptyStable by viewModel.isEmptyStable.collectAsState()

    val listState = rememberLazyListState()

    val themeOverrideManager = LocalThemeOverrideManager.current

    val posterPalette = librariesData.firstOrNull()?.props.let {
        when (it) {
            is NMHomeCardWrapper -> it.data?.colorPalette?.poster
            else -> null
        }
    }
    val focusColor = remember(posterPalette) { pickPaletteColor(posterPalette) }
    val key = remember { UUID.randomUUID() }

    DisposableEffect(focusColor) {
        themeOverrideManager.add(key, focusColor)

        onDispose {
            themeOverrideManager.remove(key)
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
                        Text(stringResource(R.string.try_again))
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when {
                    isLoading -> {
                        // Skip LazyColumn entirely
                    }
                    isEmptyStable -> {
                        item {
                            EmptyGrid(
                                modifier = Modifier.fillMaxSize(),
                                text = "No content available in this library."
                            )
                        }
                    }
                    else -> {
                        items(librariesData, key = { it.id }) { component ->
                            key(component.id) {
                                NMComponent(
                                    components = listOf(component),
                                    navController = navController,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}