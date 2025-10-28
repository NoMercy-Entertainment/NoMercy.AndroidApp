package tv.nomercy.app.views.base.search.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import tv.nomercy.app.R
import tv.nomercy.app.components.PosterBackground
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchType.Video) }

    val imeVisible = WindowInsets.isImeVisible
    val hasResults = searchQuery.isNotBlank()

    val positionState = when {
        imeVisible -> SearchBarPosition.Open
        hasResults && !imeVisible -> SearchBarPosition.Contents
        else -> SearchBarPosition.Closed
    }

    val screenHeightDp = LocalWindowInfo.current.containerSize.height.dp
    val searchBarHeight = 60.dp

    val searchBarOffset = when (positionState) {
        SearchBarPosition.Closed -> screenHeightDp / 8.1f + 1.dp
        SearchBarPosition.Open -> screenHeightDp / 6.1f + 1.dp
        SearchBarPosition.Contents -> screenHeightDp / 3.6f - 5.dp
    }

    val padding = when (positionState) {
        SearchBarPosition.Closed -> 16.dp
        SearchBarPosition.Open -> 0.dp
        SearchBarPosition.Contents -> 0.dp
    }

    val clampedOffset = searchBarOffset.coerceAtMost(screenHeightDp - searchBarHeight - 16.dp)
    val animatedOffset by animateDpAsState(targetValue = clampedOffset, label = "SearchBarOffset")

    PosterBackground(
        visible = searchQuery.isBlank()
    )

    Box(
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
    ) {

        GreetingHeader(
            searchQuery = searchQuery,
            selectedFilter = selectedFilter
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .offset(y = animatedOffset)
        ) {
            SearchBarWithToggle(
                searchQuery = searchQuery,
                positionState = positionState,
                onQueryChange = { searchQuery = it },
                selectedType = selectedFilter,
                onTypeChange = { type ->
                    selectedFilter = when (type) {
                        SearchType.Music -> SearchType.Music
                        SearchType.Video -> SearchType.Video
                    }
                },
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
//            .padding(bottom = 96.dp)
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))
    }
}

enum class SearchBarPosition { Closed, Open, Contents }
enum class SearchType { Video, Music }

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
                .padding(top = 64.dp),
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
private fun PopularContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            SearchSection(title = "Trending Now") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(6) { index ->
                        TrendingCard(
                            title = "Trending ${index + 1}",
                            type = if (index % 2 == 0) "Movie" else "TV Show",
                            trending = "#${index + 1}"
                        )
                    }
                }
            }
        }

        item {
            SearchSection(title = "Popular Genres") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(8) { index ->
                        GenreCard(
                            name = when (index) {
                                0 -> "Action"
                                1 -> "Comedy"
                                2 -> "Drama"
                                3 -> "Sci-Fi"
                                4 -> "Horror"
                                5 -> "Romance"
                                6 -> "Thriller"
                                else -> "Documentary"
                            },
                            count = "${(index + 1) * 15} items"
                        )
                    }
                }
            }
        }

        item {
            SearchSection(title = "Recent Searches") {
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(5) { index ->
                        RecentSearchItem(
                            query = when (index) {
                                0 -> "Breaking Bad"
                                1 -> "Marvel"
                                2 -> "The Beatles"
                                3 -> "Christopher Nolan"
                                else -> "Search ${index + 1}"
                            },
                            type = when (index) {
                                0 -> "TV Show"
                                1 -> "Movies"
                                2 -> "Music"
                                3 -> "Director"
                                else -> "Mixed"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResults(query: String, filter: String) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Results for \"$query\"",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        when (filter) {
            "All" -> {
                item {
                    SearchSection(title = "Movies") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(4) { index ->
                                SearchResultCard(
                                    title = "$query Movie ${index + 1}",
                                    subtitle = "202${index + 0}",
                                    type = "Movie"
                                )
                            }
                        }
                    }
                }

                item {
                    SearchSection(title = "TV Shows") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(3) { index ->
                                SearchResultCard(
                                    title = "$query Series ${index + 1}",
                                    subtitle = "${index + 1} Seasons",
                                    type = "TV Show"
                                )
                            }
                        }
                    }
                }

                item {
                    SearchSection(title = "Music") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(2) { index ->
                                SearchResultCard(
                                    title = "$query Album ${index + 1}",
                                    subtitle = "Artist ${index + 1}",
                                    type = "Music"
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.height(600.dp)
                    ) {
                        items(12) { index ->
                            SearchResultCard(
                                title = "$query $filter ${index + 1}",
                                subtitle = if (filter == "Movies") "202${index % 5}"
                                         else if (filter == "TV Shows") "${index + 1} Seasons"
                                         else if (filter == "Music") "Artist ${index + 1}"
                                         else "Actor/Director",
                                type = filter
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun TrendingCard(
    title: String,
    type: String,
    trending: String
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { /* Handle click */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    painter = painterResource(R.drawable.collection),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = trending,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GenreCard(
    name: String,
    count: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { /* Handle click */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = count,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun RecentSearchItem(
    query: String,
    type: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { /* Handle click */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.clock),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = query,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    title: String,
    subtitle: String,
    type: String
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { /* Handle click */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (type == "Music") 120.dp else 180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    painter = painterResource(
                        when (type) {
                            "Music" -> R.drawable.lyrics
                            "People" -> R.drawable.user
                            else -> R.drawable.collection
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

