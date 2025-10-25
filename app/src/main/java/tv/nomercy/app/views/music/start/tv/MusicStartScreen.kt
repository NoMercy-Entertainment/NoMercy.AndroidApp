package tv.nomercy.app.views.music.start.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.nomercy.app.components.BackdropImageWithOverlay
import tv.nomercy.app.components.ColumnFocusRouter
import tv.nomercy.app.components.EmptyGrid
import tv.nomercy.app.components.VideoMusicHeroSection
import tv.nomercy.app.components.LoadingIndicator
import tv.nomercy.app.components.nMComponents.NMComponent
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMCarouselProps
import tv.nomercy.app.shared.models.NMMusicCardProps
import tv.nomercy.app.shared.models.NMMusicHomeCardProps
import tv.nomercy.app.shared.stores.AuthStore
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.LocalNavbarFocusBridge
import tv.nomercy.app.shared.ui.LocalOnActiveCardChange2
import tv.nomercy.app.shared.ui.LocalOnActiveRowInColumn
import tv.nomercy.app.shared.ui.LocalRegisterRowFocusController
import tv.nomercy.app.shared.ui.LocalRequestFocusAt
import tv.nomercy.app.shared.ui.LocalRequestFocusAtFirstVisible
import tv.nomercy.app.shared.ui.LocalRequestFocusAtViewportX
import tv.nomercy.app.shared.ui.LocalRowIndex
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.ui.RowFocusController
import tv.nomercy.app.shared.utils.pickPaletteColor
import tv.nomercy.app.views.base.home.mobile.hasContent
import tv.nomercy.app.views.music.start.shared.MusicStartViewModel
import tv.nomercy.app.views.music.start.shared.MusicStartViewModelFactory
import java.util.UUID

@Composable
fun MusicStartScreen(navController: NavController) {
    val context = LocalContext.current
    val authStore = remember { GlobalStores.getAuthStore(context) }
    val viewModel: MusicStartViewModel = viewModel(
        factory = MusicStartViewModelFactory(
            musicStartStore = GlobalStores.getMusicStartStore(context),
            authStore = authStore
        )
    )

    val musicStartData by viewModel.musicStartData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isEmptyStable by viewModel.isEmptyStable.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val filteredData = remember(musicStartData) { musicStartData.filter(::hasContent) }

    val firstItem = remember(filteredData) {
        ((filteredData.firstOrNull()?.props as? NMCarouselProps)
            ?.items?.firstOrNull()?.props as? NMMusicHomeCardProps)
        ?.data
    }

    val heroHeight = 336.dp
    val overlap = 72.dp

    val activeCardState = remember { mutableStateOf<NMMusicCardProps?>(null) }
    val debouncedSelectedCard = remember { mutableStateOf(firstItem) }

    LaunchedEffect(filteredData) { activeCardState.value = null }

    LaunchedEffect(firstItem) {
        if (activeCardState.value == null) {
            debouncedSelectedCard.value = firstItem
        }
    }

    val systemAppConfigStore = GlobalStores.getAppConfigStore(context)
    val useAutoThemeColors by systemAppConfigStore.useAutoThemeColors.collectAsState()

    val fallbackColor = MaterialTheme.colorScheme.primary
    val primary = MaterialTheme.colorScheme.primary
    val focusColor by remember {
        derivedStateOf {
            if (!useAutoThemeColors) fallbackColor
            else pickPaletteColor(debouncedSelectedCard.value?.colorPalette?.cover, fallbackColor = primary)
        }
    }

    val themeOverrideManager = LocalThemeOverrideManager.current
    val themeKey = remember { UUID.randomUUID() }
    val themeScope = rememberCoroutineScope()

    DisposableEffect(focusColor) {
        val job = themeScope.launch {
            withFrameNanos {}
            themeOverrideManager.add(themeKey, focusColor)
        }
        onDispose {
            job.cancel()
            themeOverrideManager.remove(themeKey)
        }
    }

    LaunchedEffect(activeCardState.value) {
        delay(300)
        debouncedSelectedCard.value = activeCardState.value ?: firstItem
    }

    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
    ) {
        BackdropImageWithOverlay(imageUrl = debouncedSelectedCard.value?.cover)

        Box(modifier = Modifier.fillMaxSize()) {
            VideoMusicHeroSection(debouncedSelectedCard.value, heroHeight)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = heroHeight - overlap)
                    .zIndex(1f)
            ) {
                when {
                    isLoading -> LoadingIndicator()
                    isEmptyStable -> EmptyGrid(
                        modifier = Modifier.fillMaxSize(),
                        text = "No content available in this library."
                    )
                    filteredData.isNotEmpty() -> ContentList(
                        filteredData = filteredData,
                        navController = navController,
                        listState = listState,
                        activeCardState = activeCardState,
                        firstItem = firstItem,
                        authStore = authStore
                    )
                }
            }
        }
    }
}

@Composable
fun ContentList(
    filteredData: List<Component>,
    navController: NavController,
    listState: LazyListState,
    activeCardState: MutableState<NMMusicCardProps?>,
    firstItem: NMMusicCardProps?,
    authStore: AuthStore
) {
    val rowControllers = remember { mutableMapOf<Int, RowFocusController>() }
    val topAlignOffsetPx = with(LocalDensity.current) { 0.dp.roundToPx() }
    val router = remember(filteredData, rowControllers.size) {
        ColumnFocusRouter(listState, rowControllers, filteredData, topAlignOffsetPx)
    }

    val navbarBridge = LocalNavbarFocusBridge.current
    val initialFocusRequested = remember { mutableStateOf(false) }

    LaunchedEffect(filteredData) {
        initialFocusRequested.value = false
    }

    LaunchedEffect(filteredData, rowControllers.size) {
        navbarBridge.focusFirstInContent = suspend { router.focusFirstInContent() }
        if (!initialFocusRequested.value && filteredData.isNotEmpty()) {
            router.requestInitialFocus()
            initialFocusRequested.value = true
        }
    }

    val snapFling = rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Start
    )

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        flingBehavior = snapFling,
    ) {
        itemsIndexed(filteredData, key = { index, item -> item.id }) { rowIndex, component ->
            CompositionLocalProvider(
                LocalOnActiveCardChange2 provides { card -> activeCardState.value = card },
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    LaunchedEffect(Unit) {
        authStore.markReady()
    }
}
