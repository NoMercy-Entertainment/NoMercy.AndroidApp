package tv.nomercy.app.views.base.search.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import tv.nomercy.app.R
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.MoooomIconName
import tv.nomercy.app.components.PosterBackground
import tv.nomercy.app.components.images.AppLogoSquare
import tv.nomercy.app.components.nMComponents.NMComponent
import tv.nomercy.app.components.nMComponents.hasContent
import tv.nomercy.app.shared.models.SearchResultElement
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.views.base.search.shared.SearchBarPosition
import tv.nomercy.app.views.base.search.shared.SearchType
import tv.nomercy.app.views.base.search.shared.SearchViewModel
import tv.nomercy.app.views.base.search.shared.SearchViewModelFactory
import java.time.LocalTime

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun SearchScreen(navController: NavHostController) {

    val context = LocalContext.current
    val viewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(
            searchStore = GlobalStores.getSearchStore(context),
        )
    )

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.error.collectAsState()
    val musicResults by viewModel.musicResults.collectAsStateWithLifecycle()
    val videoResults by viewModel.videoResults.collectAsStateWithLifecycle()
    val searchType by viewModel.searchType.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val imeVisible = WindowInsets.isImeVisible
    val hasResults = musicResults.isNotEmpty() || videoResults.isNotEmpty()

    val positionState = when {
        imeVisible -> SearchBarPosition.Open
        hasResults && !imeVisible -> SearchBarPosition.Contents
        else -> SearchBarPosition.Closed
    }

    val contentAlignment = when (positionState) {
        SearchBarPosition.Closed -> Alignment.Center
        SearchBarPosition.Open -> Alignment.BottomCenter
        SearchBarPosition.Contents -> Alignment.BottomCenter
    }

    val padding = when (positionState) {
        SearchBarPosition.Closed -> PaddingValues(top = 300.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        SearchBarPosition.Open -> PaddingValues(0.dp)
        SearchBarPosition.Contents -> PaddingValues(0.dp)
    }

    val paddingBottom = when (positionState) {
        SearchBarPosition.Closed -> 0.dp
        SearchBarPosition.Open -> 58.dp
        SearchBarPosition.Contents -> 58.dp
    }

    val listState = rememberLazyListState()

    Scaffold { contentPadding ->

        PosterBackground(
            visible = searchQuery.isBlank()
        )

        Box(
            modifier = Modifier
                .consumeWindowInsets(
                    paddingValues = PaddingValues(
                        top = contentPadding.calculateTopPadding(),
                        start = contentPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = contentPadding.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = contentPadding.calculateBottomPadding() + 52.dp
                    )
                )
                .imePadding()
                .fillMaxSize()
        ) {

            GreetingHeader(
                searchQuery = searchQuery,
                selectedFilter = searchType
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .zIndex(1f),
                contentAlignment = contentAlignment
            ) {
                SearchBarWithToggle(
                    searchQuery = searchQuery,
                    positionState = positionState,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    selectedType = searchType,
                    onTypeChange = { type -> viewModel.onSearchTypeChanged(type) },
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f)
                    .padding(bottom = paddingBottom)
                    .padding(horizontal = 16.dp)
            ) {

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
                                //                        viewModel.clearError()
                                //                        viewModel.refresh()
                            }) {
                                Text(stringResource(R.string.try_again))
                            }
                        }
                    }
                }

                when {
                    searchType == SearchType.Music && musicResults.isNotEmpty() -> {
                        val filteredData =
                            musicResults.filter { component -> hasContent(component) }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
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
                    }

                    searchType == SearchType.Video && videoResults.isNotEmpty() -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 20.dp,
                                bottom = 20.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(videoResults, key = { it.id }) { data ->
                                key(data.id) {
                                    SearchResultCard(
                                        item = data,
                                        onClick = { route -> navController.navigate(route) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    isLoading -> {
                        // Only show loading if we don't have data
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
    }
}


@Composable
fun SearchBarWithToggle(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    selectedType: SearchType,
    onTypeChange: (SearchType) -> Unit,
    modifier: Modifier = Modifier,
    positionState: SearchBarPosition
) {
    val transition = updateTransition(targetState = selectedType, label = "ToggleTransition")
    val indicatorOffset by transition.animateDp(label = "IndicatorOffset") { type ->
        when (type) {
            SearchType.Video -> 2.dp
            SearchType.Music -> 54.dp
        }
    }

    val placeholder = when (selectedType) {
        SearchType.Video -> stringResource(R.string.placeholder_video)
        SearchType.Music -> stringResource(R.string.placeholder_music)
    }

    val radius = when (positionState) {
        SearchBarPosition.Closed -> 16.dp
        SearchBarPosition.Open -> 0.dp
        SearchBarPosition.Contents -> 0.dp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(shape = RoundedCornerShape(radius))
            .background(color = MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(radius)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(width = 120.dp)
                    .fillMaxHeight()
                    .padding(6.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    .zIndex(1f)
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .fillMaxHeight()
                        .width(52.dp)
                        .padding(horizontal = 2.dp, vertical = 4.dp)
                        .clip(shape = RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.90f))
                        .align(Alignment.CenterStart)
                )
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onTypeChange(SearchType.Video) },
                        modifier = Modifier
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.filmmedia),
                            contentDescription = "Video",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onTypeChange(SearchType.Music) },
                        modifier = Modifier
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.notedouble),
                            contentDescription = "Music",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                    }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.7f
                    ),
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.7f
                    )
                )
            )
        }
    }
}

@Composable
fun GreetingHeader(
    searchQuery: String,
    selectedFilter: SearchType,
) {
    AnimatedVisibility(
        visible = searchQuery.isBlank(),
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 92.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = greetingString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                id = if (selectedFilter == SearchType.Video) R.string.prompt_video else R.string.prompt_music),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun greetingString(): String {
    val hour = LocalTime.now().hour
    val resId = when (hour) {
        in 6..11 -> R.string.greeting_morning
        in 12..17 -> R.string.greeting_afternoon
        in 18..23 -> R.string.greeting_evening
        else -> R.string.greeting_night
    }
    return stringResource(id = resId)
}

@Composable
fun SearchResultCard(
    item: SearchResultElement,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageBaseUrl = "https://image.tmdb.org/t/p/original"
    val title = item.name ?: item.title ?: ""
    val year = item.releaseDate ?: item.firstAirDate
    val parsedYear = year?.take(4)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick("/${item.mediaType}/${item.id}") }
            .background(Color.Black)
    ) {
        // Backdrop image
        item.backdropPath?.let { path ->
            AsyncImage(
                model = "$imageBaseUrl$path",
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        // Gradient overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 1000f
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Poster/profile image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .aspectRatio(2f / 3f)
                    .background(Color.Black)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                val posterUrl = item.posterPath ?: item.profilePath
                if (posterUrl != null) {
                    AsyncImage(
                        model = "$imageBaseUrl$posterUrl",
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else {
                    AppLogoSquare(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            // Text column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(3f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val icon = when (item.mediaType) {
                        "tv" -> MoooomIconName.Tv
                        "movie" -> MoooomIconName.FilmMedia
                        "person" -> MoooomIconName.Person
                        else -> MoooomIconName.Collection1
                    }

                    MoooomIcon(
                        icon = icon,
                        contentDescription = "Devices",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )

                    parsedYear?.let {
                        Text(
                            text = "($it)",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                        )
                    }
                }

                item.overview?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}