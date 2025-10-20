package tv.nomercy.app.views.base.libraries.tv

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.isTv
import tv.nomercy.app.shared.utils.pickPaletteColor
import tv.nomercy.app.views.base.home.mobile.hasContent
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
            is NMHomeCardWrapper -> if (isTv()) it.data?.colorPalette?.backdrop else it.data?.colorPalette?.poster
            else -> null
        }
    }
    val focusColor = remember(posterPalette) { pickPaletteColor(posterPalette) }
    val key = remember { UUID.randomUUID() }

    val scope = rememberCoroutineScope()
    DisposableEffect(focusColor) {
        val job = scope.launch {
            withFrameNanos {
                themeOverrideManager.add(key, focusColor)
            }
        }

        onDispose {
            job.cancel()
            themeOverrideManager.remove(key)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        errorMessage?.let {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = it)
                    Button(onClick = { viewModel.refresh() }) {
                        Text(text = stringResource(R.string.try_again))
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(top = 72.dp)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                    lazyListState = listState,
                    snapPosition = androidx.compose.foundation.gestures.snapping.SnapPosition.Start
                ),
            ) {
                when {
                    librariesData.isNotEmpty() -> {
                        val filteredData = librariesData.filter { component -> hasContent(component) }

                        items(filteredData, key = { it.id }) { component ->
                            key(component.id) {
                                NMComponent(
                                    components = listOf(component),
                                    navController = navController,
                                    modifier = Modifier.fillMaxWidth(),
                                    aspectRatio = AspectRatio.Backdrop,
                                )
                            }
                        }
                    }

                    isLoading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    isEmptyStable -> {
                        item {
                            EmptyGrid(
                                modifier = Modifier.fillMaxSize(),
                                text = "No content available in this library."
                            )
                        }
                    }
                }
            }
        }
    }
}