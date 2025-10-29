package tv.nomercy.app.views.base.libraries.tv

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import tv.nomercy.app.components.ColumnFocusRouter
import tv.nomercy.app.components.EmptyGrid
import tv.nomercy.app.components.ErrorMessage
import tv.nomercy.app.components.LoadingIndicator
import tv.nomercy.app.components.nMComponents.NMComponent
import tv.nomercy.app.components.nMComponents.hasContent
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMHomeCardWrapper
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.LocalNavbarFocusBridge
import tv.nomercy.app.shared.ui.LocalOnActiveRowInColumn
import tv.nomercy.app.shared.ui.LocalRegisterRowFocusController
import tv.nomercy.app.shared.ui.LocalRequestFocusAt
import tv.nomercy.app.shared.ui.LocalRequestFocusAtFirstVisible
import tv.nomercy.app.shared.ui.LocalRequestFocusAtViewportX
import tv.nomercy.app.shared.ui.LocalRowIndex
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.ui.RowFocusController
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.isTv
import tv.nomercy.app.shared.utils.pickPaletteColor
import tv.nomercy.app.views.base.libraries.shared.LibrariesViewModel
import tv.nomercy.app.views.base.libraries.shared.LibrariesViewModelFactory
import java.util.UUID

@Composable
fun LibrariesScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: LibrariesViewModel = viewModel(
        factory = LibrariesViewModelFactory(GlobalStores.getLibrariesStore(context))
    )

    val librariesData by viewModel.librariesData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isEmptyStable by viewModel.isEmptyStable.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    val isTv = isTv()
    val posterPalette = remember(librariesData) {
        (librariesData.firstOrNull()?.props as? NMHomeCardWrapper)?.let {
            if (isTv) it.data?.colorPalette?.backdrop else it.data?.colorPalette?.poster
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    val focusColor by remember {
        derivedStateOf {
            pickPaletteColor(posterPalette, fallbackColor = primary)
        }
    }

    val themeOverrideManager = LocalThemeOverrideManager.current
    val themeKey = remember { UUID.randomUUID() }
    val themeScope = rememberCoroutineScope()


    val rowControllers = remember { mutableMapOf<Int, RowFocusController>() }
    val router = remember(librariesData, rowControllers.size) {
        ColumnFocusRouter(listState, rowControllers, librariesData)
    }

    DisposableEffect(focusColor) {
        val job = themeScope.launch {
            withFrameNanos { themeOverrideManager.add(themeKey, focusColor) }
        }
        onDispose {
            job.cancel()
            themeOverrideManager.remove(themeKey)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        errorMessage?.let {
            ErrorMessage(message = it, onRetry = viewModel::refresh)
            return@Column
        }

        FocusRoutingSetup(librariesData, listState)

        Box(modifier = Modifier.fillMaxSize().padding(top = 72.dp)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                flingBehavior = rememberSnapFlingBehavior(
                    lazyListState = listState,
                    snapPosition = SnapPosition.Start
                ),
            ) {
                when {
                    librariesData.isNotEmpty() -> {
                        itemsIndexed(librariesData, key = { index, item -> item.id }) { rowIndex, component ->
                            CompositionLocalProvider(
                                LocalOnActiveRowInColumn provides suspend { router.alignRow(rowIndex) },
                                LocalRowIndex provides rowIndex,
                                LocalRegisterRowFocusController provides router::registerController,
                                LocalRequestFocusAt provides router::focusAt,
                                LocalRequestFocusAtViewportX provides router::focusAtViewportX,
                                LocalRequestFocusAtFirstVisible provides router::focusFirstVisible,
                            ) {
                                NMComponent(
                                    components = listOf(component),
                                    navController = navController,
                                    modifier = Modifier.fillMaxWidth(),
                                    aspectRatio = AspectRatio.Backdrop,
                                )
                            }
                        }
                    }

                    isLoading -> item { LoadingIndicator() }
                    isEmptyStable -> item {
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


@Composable
private fun FocusRoutingSetup(
    filteredData: List<Component>,
    listState: LazyListState
) {
    val rowControllers = remember { mutableMapOf<Int, RowFocusController>() }
    val topAlignOffsetPx = with(LocalDensity.current) { (-16).dp.roundToPx() }
    val router = remember(filteredData, rowControllers.size) {
        ColumnFocusRouter(listState, rowControllers, filteredData, topAlignOffsetPx)
    }

    val navbarBridge = LocalNavbarFocusBridge.current
    val initialFocusRequested = remember { mutableStateOf(false) }

    LaunchedEffect(filteredData) { initialFocusRequested.value = false }

    LaunchedEffect(filteredData, rowControllers.size) {
        navbarBridge.focusFirstInContent = suspend { router.focusFirstInContent() }
        if (!initialFocusRequested.value && filteredData.isNotEmpty()) {
            router.requestInitialFocus()
            initialFocusRequested.value = true
        }
    }
}