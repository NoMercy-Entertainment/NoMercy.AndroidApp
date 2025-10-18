package tv.nomercy.app.views.base.home.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tv.nomercy.app.views.base.home.shared.HomeViewModel
import tv.nomercy.app.views.base.home.shared.HomeViewModelFactory
import tv.nomercy.app.views.base.home.mobile.hasContent
import tv.nomercy.app.shared.components.EmptyGrid
import tv.nomercy.app.shared.components.SplitTitleText
import tv.nomercy.app.shared.components.TMDBImage
import tv.nomercy.app.components.nMComponents.NMComponent
import tv.nomercy.app.shared.models.NMCardWrapper
import tv.nomercy.app.shared.models.NMCarouselProps
import tv.nomercy.app.shared.models.NMCardProps
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import tv.nomercy.app.shared.ui.LocalOnActiveCardChange
import tv.nomercy.app.shared.ui.LocalRegisterRowFocusController
import tv.nomercy.app.shared.ui.LocalRequestFocusAt
import tv.nomercy.app.shared.ui.LocalRowIndex
import tv.nomercy.app.shared.ui.RowFocusController
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType
import tv.nomercy.app.shared.ui.LocalNavbarFocusBridge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween

@Composable
fun TvHomeScreen(navController: NavHostController) {

    val context = LocalContext.current
    val factory = remember {
        HomeViewModelFactory(
            homeStore = GlobalStores.getHomeStore(context),
            authStore = GlobalStores.getAuthStore(context)
        )
    }
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val authStore = GlobalStores.getAuthStore(LocalContext.current)

    val homeData by viewModel.homeData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isEmptyStable by viewModel.isEmptyStable.collectAsState()

    val listState = rememberLazyListState()

    val firstItem = homeData.elementAtOrNull(0)?.props?.let { props ->
        when (props) {
            is NMCarouselProps -> (props.items.elementAtOrNull(0)?.props as? NMCardWrapper)?.data
            else -> null
        }
    }

    val heroHeight = 336.dp
    val overlap = 72.dp

    // Track the currently active card (focused/hovered). Falls back to firstItem when null.
    val activeCardState = androidx.compose.runtime.remember { mutableStateOf<NMCardProps?>(null) }

    // Debounced selected card for hero/backdrop to avoid thrashing while scrolling
    val debouncedSelectedCard = androidx.compose.runtime.remember { mutableStateOf<NMCardProps?>(firstItem) }

    // Reset active card when the home data changes to ensure a sensible default
    androidx.compose.runtime.LaunchedEffect(homeData) { activeCardState.value = null }

    // Keep the debounced card in sync with firstItem when no active card
    androidx.compose.runtime.LaunchedEffect(firstItem) {
        if (activeCardState.value == null) {
            debouncedSelectedCard.value = firstItem
        }
    }

    // Debounce hero/background updates so carousel scrolling takes priority
    androidx.compose.runtime.LaunchedEffect(activeCardState.value, firstItem) {
        val target = activeCardState.value ?: firstItem
        // Short debounce to let flings/scroll settle
        kotlinx.coroutines.delay(16)
        debouncedSelectedCard.value = target
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        BackdropImageWithOverlay(
            imageUrl = debouncedSelectedCard.value?.backdrop,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            HeroRow(
                title = debouncedSelectedCard.value?.title,
                overview = debouncedSelectedCard.value?.overview,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
                    .align(Alignment.TopStart)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = heroHeight - overlap)
                    .zIndex(1f)
            ) {
                when {
                    homeData.isNotEmpty() -> {
                        val filteredData = homeData.filter { component -> hasContent(component) }

                        // Registry of per-row focus controllers to route vertical D-pad focus deterministically
                        val rowControllers = remember { mutableMapOf<Int, RowFocusController>() }

                        // Build a router delegate to centralize focus-related actions for the column
                        val router = remember(filteredData, rowControllers.size) { ColumnFocusRouter(listState, rowControllers, filteredData) }

                        // Register a handler so the navbar can send focus into the first focusable element of this screen
                        val navbarBridge = LocalNavbarFocusBridge.current
                        LaunchedEffect(filteredData, rowControllers.size) {
                            navbarBridge.focusFirstInContent = suspend {
                                router.focusFirstInContent()
                            }
                        }

                        // Ensure first card becomes focused when data arrives
                        val initialFocusRequested = remember { mutableStateOf(false) }
                        LaunchedEffect(homeData) { initialFocusRequested.value = false }
                        LaunchedEffect(filteredData, rowControllers.size) {
                            if (!initialFocusRequested.value && filteredData.isNotEmpty()) {
                                router.requestInitialFocus()
                                initialFocusRequested.value = true
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
//                            contentPadding = PaddingValues(start = 28.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                                lazyListState = listState,
                                snapPosition = androidx.compose.foundation.gestures.snapping.SnapPosition.Start
                            ),
                        ) {
                            itemsIndexed(filteredData, key = { index, item -> item.id }) { rowIndex, component ->
                                CompositionLocalProvider(
                                    LocalOnActiveCardChange provides { card -> activeCardState.value = card },
                                    tv.nomercy.app.shared.ui.LocalOnActiveRowInColumn provides suspend { router.alignRow(rowIndex) },
                                    LocalRowIndex provides rowIndex,
                                    LocalRegisterRowFocusController provides router::registerController,
                                    LocalRequestFocusAt provides router::focusAt,
                                    tv.nomercy.app.shared.ui.LocalRequestFocusAtViewportX provides router::focusAtViewportX,
                                    tv.nomercy.app.shared.ui.LocalRequestFocusAtFirstVisible provides router::focusFirstVisible,
                                ) {
                                    NMComponent(
                                        components = listOf(component),
                                        navController = navController,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                        authStore.markReady()
                    }

                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    isEmptyStable -> {
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
fun HeroRow(
    title: String?,
    overview: String?,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LeftColumn(title = title, overview = overview, modifier = Modifier.weight(3f))
        RightColumn(modifier = Modifier.weight(2f))
    }
}

@Composable
fun LeftColumn(title: String?, overview: String?, modifier: Modifier = Modifier) {
    // Reserve fixed heights for title and overview blocks to prevent layout shift
    // Title block: accommodates one main line (26sp lineHeight) plus optional subtitle (22sp) and 4dp spacing
    // Overview block: 4 lines at 24sp lineHeight
    val titleBlockHeight = 56.dp
    val overviewBlockHeight = 104.dp

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(start = 40.dp, end = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.height(titleBlockHeight).fillMaxWidth()) {
            Crossfade(targetState = title, animationSpec = tween(durationMillis = 200), label = "title-fade") { t ->
                if (!t.isNullOrBlank()) {
                    SplitTitleText(
                        title = t,
                        mainStyle = MaterialTheme.typography.headlineMedium
                            .copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                lineHeight = 26.sp
                            ),
                        subtitleStyle = MaterialTheme.typography.headlineSmall
                            .copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                lineHeight = 22.sp
                            ),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.height(overviewBlockHeight).fillMaxWidth()) {
            Crossfade(targetState = overview, animationSpec = tween(durationMillis = 200), label = "overview-fade") { o ->
                if (!o.isNullOrBlank()) {
                    Text(
                        text = o,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp
                        ),
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun RightColumn(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
    )
}

@Composable
fun BackdropImage(imageUrl: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(start = 200.dp)
            .fillMaxWidth()
            .aspectFromType(AspectRatio.Backdrop)
            .clipToBounds()
    ) {
        TMDBImage(
            path = imageUrl,
            title = "Backdrop image for $imageUrl",
            aspectRatio = null,
            size = 1280,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}

@Composable
fun BackdropImageWithOverlay(imageUrl: String?) {
    Box(modifier = Modifier.fillMaxSize()) {

        Crossfade(
            targetState = imageUrl,
            animationSpec = tween(durationMillis = 300),
            label = "backdrop-fade"
        ) { url ->
            BackdropImage(
                imageUrl = url,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(0f)
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            val radius = maxOf(widthPx, heightPx) * 1.8f
            val centerOffset = Offset(widthPx - 225, -500f)

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = 1.15f
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                                Color.Black,
                            ),
                            center = centerOffset,
                            radius = radius
                        )
                    )
            )
        }
    }
}

// Delegate to centralize vertical focus routing and alignment for the Home TV column
/**
 * Centralizes vertical focus routing and alignment for the Home TV LazyColumn.
 *
 * Responsibilities:
 * - Maintain a registry of row focus controllers.
 * - Move focus to specific items/rows on DPAD navigation.
 * - Ensure target rows are visible and aligned before requesting item focus.
 */
private class ColumnFocusRouter(
    private val listState: androidx.compose.foundation.lazy.LazyListState,
    private val rowControllers: MutableMap<Int, RowFocusController>,
    private val data: List<tv.nomercy.app.shared.models.Component>
) {
    /**
     * Register or replace the RowFocusController for a given row index.
     */
    fun registerController(index: Int, controller: RowFocusController) {
        rowControllers[index] = controller
    }

    /**
     * Focus a specific item in a specific row. Ensures the row is brought into view first.
     */
    suspend fun focusAt(rowIndex: Int, itemIndex: Int) {
        if (rowIndex in data.indices) {
            listState.animateScrollToItem(rowIndex)
            rowControllers[rowIndex]?.focusItem?.invoke(itemIndex)
        }
    }

    /**
     * Focus the item in the target row whose start edge is closest to the given viewport X.
     * Useful for aligning vertical navigation with what is visually above/below.
     */
    suspend fun focusAtViewportX(rowIndex: Int, viewportX: Float) {
        if (rowIndex in data.indices) {
            listState.animateScrollToItem(rowIndex)
            rowControllers[rowIndex]?.focusNearestAtViewportX?.invoke(viewportX)
        }
    }

    /**
     * Move focus to the left-most fully visible item of the given row.
     * Waits for the row to be visible and laid out before delegating to the row controller.
     */
    suspend fun focusFirstVisible(rowIndex: Int) {
        if (rowIndex in data.indices) {
            listState.animateScrollToItem(rowIndex)
            var attempts = 0
            while (!listState.layoutInfo.visibleItemsInfo.any { it.index == rowIndex } && attempts < 60) {
                kotlinx.coroutines.delay(4)
                attempts++
            }
            androidx.compose.runtime.withFrameNanos { }
            rowControllers[rowIndex]?.focusFirstVisible?.invoke()
        }
    }

    /**
     * Handoff from the navbar into the screen: focus the first focusable content element.
     * Scrolls to the first row, waits for controller registration and a frame, then focuses its first visible item.
     */
    suspend fun focusFirstInContent() {
        if (data.isNotEmpty()) {
            listState.animateScrollToItem(0)
            var attempts = 0
            while (rowControllers[0] == null && attempts < 60) {
                kotlinx.coroutines.delay(4)
                attempts++
            }
            androidx.compose.runtime.withFrameNanos { }
            rowControllers[0]?.focusFirstVisible?.invoke()
        }
    }

    /**
     * Called once when data arrives to ensure the first card is focused.
     * Scrolls to row 0, waits for controller registration and a frame, then focuses item 0.
     */
    suspend fun requestInitialFocus() {
        if (data.isNotEmpty()) {
            listState.animateScrollToItem(0)
            var attempts = 0
            while (rowControllers[0] == null && attempts < 60) {
                kotlinx.coroutines.delay(4)
                attempts++
            }
            androidx.compose.runtime.withFrameNanos { }
            rowControllers[0]?.focusItem?.invoke(0)
        }
    }

    /**
     * Align the given row to the top of the viewport without animation.
     */
    suspend fun alignRow(rowIndex: Int) {
        listState.scrollToItem(rowIndex)
    }
}
