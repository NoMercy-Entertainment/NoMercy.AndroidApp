package tv.nomercy.app.views.base.info.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import tv.nomercy.app.R
import tv.nomercy.app.components.BackdropImageWithOverlay
import tv.nomercy.app.components.GenericCarousel
import tv.nomercy.app.components.HeroRow
import tv.nomercy.app.components.LinkButton
import tv.nomercy.app.components.SeasonCarousel
import tv.nomercy.app.components.toCarouselItem
import tv.nomercy.app.shared.models.InfoResponse
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.utils.pickPaletteColor
import tv.nomercy.app.shared.utils.sortByFilteredAlphabetized
import tv.nomercy.app.views.base.info.shared.InfoViewModel
import tv.nomercy.app.views.base.info.shared.InfoViewModelFactory
import java.util.UUID

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

    val context = LocalContext.current
    val systemAppConfigStore = GlobalStores.getAppConfigStore(context)
    val useAutoThemeColors by systemAppConfigStore.useAutoThemeColors.collectAsState()

    val fallbackColor = MaterialTheme.colorScheme.primary
    val focusColor: Color = remember(infoData?.colorPalette) {
        if (!useAutoThemeColors) fallbackColor
        else pickPaletteColor(infoData?.colorPalette?.poster, fallbackColor = fallbackColor)
    }
    val key = remember { UUID.randomUUID() }

    DisposableEffect(focusColor) {
        themeOverrideManager.add(key, focusColor)

        onDispose {
            themeOverrideManager.remove(key)
        }
    }

    val listState = rememberLazyListState(0, 40)
    val scope = rememberCoroutineScope()

    val heroHeight = 336.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        BackdropImageWithOverlay(
            imageUrl = infoData?.backdrop,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            HeroRow(
                title = infoData?.title,
                overview = infoData?.overview,
                maxLines = 10,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp)
                    .height(heroHeight)
                    .align(Alignment.TopStart)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = heroHeight - 50.dp)
                    .zIndex(1f)
            ) {

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                ) {
                    item {
                        LinkButton(
                            text = R.string.watch,
                            icon = R.drawable.nmplaysolid,
                            onClick = { infoData?.link?.let { navController.navigate("${infoData.link}/watch") } },
                            modifier = Modifier
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        scope.launch {
                                            listState.animateScrollToItem(0, 0)
                                        }
                                    }
                                }
                                .padding(start = 40.dp)
                        )
                    }
                    item {
                        // trailer button
                        LinkButton(
                            text = R.string.watch_trailer,
                            icon = R.drawable.playcircle,
                            onClick = { /* TODO: implement trailer navigation */ },
                            modifier = Modifier
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        scope.launch {
                                            listState.animateScrollToItem(1, 0)
                                        }
                                    }
                                }
                                .padding(start = 40.dp)
                        )
                    }
                    item {
                        // add to watch later button
                        LinkButton(
                            text = R.string.add_to_watch_list,
                            icon = R.drawable.bookmark,
                            onClick = { /* TODO: implement add to watch later navigation */ },
                            modifier = Modifier
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        scope.launch {
                                            listState.animateScrollToItem(2, 0)
                                        }
                                    }
                                }
                                .padding(start = 40.dp)
                        )
                    }
                    item {
                        SeasonCarousel(
                            seasons = infoData?.seasons ?: emptyList(),
                            navController = navController,
                            visibleCards = 4,
                            modifier = Modifier
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        scope.launch {
                                            listState.animateScrollToItem(3, 40)
                                        }
                                    }
                                }
                        )
                    }
                    item {
                        GenericCarousel(
                            title = stringResource(R.string.collections),
                            items = infoData?.collection?.map { it.toCarouselItem() } ?: emptyList(),
                            navController = navController,
                            visibleCards = 7,
                            modifier = Modifier
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        scope.launch {
                                            listState.animateScrollToItem(4, 40)
                                        }
                                    }
                                }
                        )
                    }
                    item {
                        GenericCarousel(
                            title = stringResource(R.string.cast),
                            items = infoData?.cast?.map { it.toCarouselItem() } ?: emptyList(),
                            navController = navController,
                            visibleCards = 7,
                            modifier = Modifier
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        scope.launch {
                                            listState.animateScrollToItem(5, 40)
                                        }
                                    }
                                }
                        )
                    }
                    item {
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
                            visibleCards = 7,
                            modifier = Modifier
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        scope.launch {
                                            listState.animateScrollToItem(6, 40)
                                        }
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}