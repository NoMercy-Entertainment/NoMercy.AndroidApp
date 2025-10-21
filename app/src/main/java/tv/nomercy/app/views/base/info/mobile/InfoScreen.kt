package tv.nomercy.app.views.base.info.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tv.nomercy.app.components.ContentRatingBadge
import tv.nomercy.app.components.ExpandableText
import tv.nomercy.app.components.GenericCarousel
import tv.nomercy.app.components.GenrePill
import tv.nomercy.app.components.InfoBlock
import tv.nomercy.app.components.InfoCard
import tv.nomercy.app.components.InfoWrapBlock
import tv.nomercy.app.components.LineBreak
import tv.nomercy.app.components.RatingBadge
import tv.nomercy.app.components.SeasonCarousel
import tv.nomercy.app.components.ShimmerBox
import tv.nomercy.app.components.ShimmerPill
import tv.nomercy.app.components.SplitTitleText
import tv.nomercy.app.components.TMDBImage
import tv.nomercy.app.components.toCarouselItem
import tv.nomercy.app.shared.models.InfoResponse
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.utils.formatDuration
import tv.nomercy.app.shared.utils.pickPaletteColor
import tv.nomercy.app.shared.utils.sortByFilteredAlphabetized
import java.util.UUID
import tv.nomercy.app.R
import tv.nomercy.app.views.base.info.shared.InfoViewModel
import tv.nomercy.app.views.base.info.shared.InfoViewModelFactory

@Composable
fun InfoScreen(type: String, id: String, navController: NavHostController) {
    val context = LocalContext.current
    val factory = remember {
        InfoViewModelFactory(
            infoStore = GlobalStores.getInfoStore(context),
            authStore = GlobalStores.getAuthStore(context)
        )
    }
    val viewModel: InfoViewModel = viewModel(factory = factory, key = "$type/$id")

    val infoData by viewModel.infoData.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(type, id) {
        viewModel.setInfoParams(type, id)
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
            InfoColumn(infoData, navController)
        }
    }
}

@Composable
private fun InfoColumn(infoData: InfoResponse?, navController: NavHostController) {
    val themeOverrideManager = LocalThemeOverrideManager.current
    val listState = rememberLazyListState()

    val primary = MaterialTheme.colorScheme.primary
    val posterPalette = infoData?.colorPalette?.poster
    val focusColor = remember(posterPalette) { pickPaletteColor(posterPalette, fallbackColor = primary) }
    val key = remember { UUID.randomUUID() }

    DisposableEffect(focusColor) {
        themeOverrideManager.add(key, focusColor)

        onDispose {
            themeOverrideManager.remove(key)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        item {

            TopSection(infoData, navController)

            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SplitTitleText(
                        title = infoData?.title ?: "",
                        mainStyle = MaterialTheme.typography.titleLarge
                            .copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                        subtitleStyle = MaterialTheme.typography.bodySmall
                            .copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 22.sp
                            ),
                    )

                    ExpandableText(
                        text = infoData?.overview,
                        minimizedMaxLines = 3
                    )

                    LineBreak()

                    InfoRow(infoData)

                    LineBreak()

                    GenreRow(infoData, navController)

                    infoData?.writer?.let { writer ->
                        InfoWrapBlock(
                            title = stringResource(R.string.writer),
                            data = writer.name,
                            modifier = Modifier.clickable {
                                writer.link.let { link ->
                                    navController.navigate(link)
                                }
                            }
                        )
                    }

                    infoData?.director?.let { director ->
                        InfoWrapBlock(
                            title = stringResource(R.string.director),
                            data = director.name,
                            modifier = Modifier.clickable {
                                director.link.let { link ->
                                    navController.navigate(link)
                                }
                            }
                        )
                    }

                    infoData?.keywords?.let { keywords ->
                        InfoWrapBlock(
                            title = stringResource(R.string.keywords),
                        ) {
                            keywords.forEach { keyword ->
                                val index = keywords.indexOf(keyword)
                                Text(
                                    text = keyword + if (index != infoData.keywords.size - 1) "," else "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    lineHeight = 14.sp,
                                )
                            }
                        }
                    }

                    LineBreak()
                }

                SeasonCarousel(
                    seasons = infoData?.seasons ?: emptyList(),
                    navController = navController,
                    visibleCards = 2
                )

                GenericCarousel(
                    title = stringResource(R.string.collections),
                    items = infoData?.collection?.map { it.toCarouselItem() } ?: emptyList(),
                    navController = navController,
                    visibleCards = 2
                )

                GenericCarousel(
                    title = stringResource(R.string.cast),
                    items = infoData?.cast?.map { it.toCarouselItem() } ?: emptyList(),
                    navController = navController,
                )

                val sorted = infoData?.crew?.sortByFilteredAlphabetized(
                    keySelector = { it.name },
                    valueSelector = { it.knownForDepartment },
                    sortSelector = { it.name },
                    filterSelector = { it.profile != null }
                ) ?: emptyList()

                GenericCarousel(
                    title = stringResource(R.string.crew),
                    items = sorted.map { it.toCarouselItem() },
                    navController = navController,
                )

                GenericCarousel(
                    title = stringResource(R.string.posters),
                    items = infoData?.posters?.map { it.toCarouselItem() } ?: emptyList(),
                    navController = navController,
                )

                GenericCarousel(
                    title = stringResource(R.string.backdrops),
                    items = infoData?.backdrops?.map { it.toCarouselItem() } ?: emptyList(),
                    navController = navController,
                    visibleCards = 2
                )

                GenericCarousel(
                    title = stringResource(R.string.recommendations),
                    items = infoData?.recommendations?.map { it.toCarouselItem() } ?: emptyList(),
                    navController = navController,
                )

                GenericCarousel(
                    title = stringResource(R.string.similar),
                    items = infoData?.similar?.map { it.toCarouselItem() } ?: emptyList(),
                    navController = navController,
                )
            }
        }
    }
}

@Composable
private fun TopSection(infoData: InfoResponse?, navController: NavHostController) {
    Box {

        InfoBackdropImage(infoData)

        InfoCard(infoData, navController)
    }
}

@Composable
private fun InfoRow(infoData: InfoResponse?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {

        ContentRatingBadge(
            ratings = infoData?.contentRatings ?: emptyList(),
            modifier = Modifier.padding(vertical = 2.dp),
            size = 4.dp
        )

        if (infoData?.collection?.isNotEmpty() == true) {
            InfoBlock(
                data = "${infoData.collection.minOf { it.year ?: 0 }} - ${infoData.collection.maxOf { it.year ?: 0 }}"
            )
        }
        else if (infoData?.year != null && infoData.year > 0) {
            InfoBlock(
                data = infoData.year.toString()
            )
        }

        InfoBlock(
            data = infoData?.numberOfItems?.let {
                infoData.haveItems.toString() + "/" + infoData.numberOfItems.toString()
            },
        )

        if (infoData?.duration != null && infoData.duration > 0) {
            InfoBlock(
                data = formatDuration(infoData.duration),
            )
        } else if (infoData?.totalDuration != null && infoData.totalDuration > 0) {
            InfoBlock(
                data = formatDuration(infoData.totalDuration),
            )
        }

        infoData?.voteAverage?.let {
            InfoBlock(
                bodyContent = {
                    RatingBadge(rating = infoData.voteAverage)
                }
            )
        }
    }
}

@Composable
private fun InfoBackdropImage(infoData: InfoResponse?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RectangleShape)
            .height(440.dp)
            .padding(bottom = 70.dp),
        contentAlignment = Alignment.Center
    ) {
        if (infoData?.backdrop != null) {
            TMDBImage(
                modifier = Modifier.matchParentSize(),
                path = infoData.backdrop,
                title = infoData.title,
                aspectRatio = null,
                size = 800,
            )
        } else {
            ShimmerBox(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.DarkGray)
            )
        }
        Box(
            Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
        )
    }
}


@Composable
private fun GenreRow(
    infoData: InfoResponse?,
    navController: NavHostController
) {
    InfoWrapBlock(
        title = stringResource(R.string.genres),
        bodyContent = {
            val genres = infoData?.genres ?: emptyList()

            if (genres.isEmpty()) {
                repeat(4) {
                    ShimmerPill(modifier = Modifier)
                }
            } else {
                genres.forEach { genre ->
                    GenrePill(
                        title = genre.name,
                        modifier = Modifier.clickable {
                            genre.link.let { link ->
                                navController.navigate(link)
                            }
                        }
                    )
                }
            }
        }
    )
}
