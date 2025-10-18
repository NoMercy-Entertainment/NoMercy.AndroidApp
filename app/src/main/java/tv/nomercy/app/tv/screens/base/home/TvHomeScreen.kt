package tv.nomercy.app.tv.screens.base.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import tv.nomercy.app.mobile.screens.base.home.HomeViewModel
import tv.nomercy.app.mobile.screens.base.home.HomeViewModelFactory
import tv.nomercy.app.mobile.screens.base.home.hasContent
import tv.nomercy.app.shared.components.EmptyGrid
import tv.nomercy.app.shared.components.SplitTitleText
import tv.nomercy.app.shared.components.TMDBImage
import tv.nomercy.app.shared.components.nMComponents.NMComponent
import tv.nomercy.app.shared.models.NMCardWrapper
import tv.nomercy.app.shared.models.NMCarouselProps
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.utils.AspectRatio
import tv.nomercy.app.shared.utils.aspectFromType

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

    val firstItem = homeData.elementAtOrNull(1)?.props?.let { props ->
        when (props) {
            is NMCarouselProps -> (props.items.elementAtOrNull(1)?.props as? NMCardWrapper)?.data
            else -> null
        }
    }

    val heroHeight = 336.dp
    val overlap = 72.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        BackdropImageWithOverlay(
            imageUrl = firstItem?.backdrop,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            HeroRow(
                title = firstItem?.title,
                overview = firstItem?.overview,
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

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 44.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(filteredData, key = { it.id }) { component ->
                                key(component.id) {
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
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(start = 56.dp, end = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        title?.let {
            SplitTitleText(
                title = it,
                mainStyle = MaterialTheme.typography.headlineMedium
                    .copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        lineHeight = 26.sp
                    ),
                subtitleStyle = MaterialTheme.typography.headlineSmall
                    .copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    ),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        overview?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
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

        BackdropImage(
            imageUrl = imageUrl,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(0f)
        )

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