package tv.nomercy.app.mobile.screens.base.libraries

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
import tv.nomercy.app.shared.components.EmptyGrid
import tv.nomercy.app.shared.components.Indexer
import tv.nomercy.app.shared.components.nMComponents.NMComponent
import tv.nomercy.app.shared.models.NMCardWrapper
import tv.nomercy.app.shared.models.NMHomeCardWrapper
import tv.nomercy.app.shared.models.NMGridProps
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.utils.pickPaletteColor
import java.util.UUID

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

    // StateFlows used to drive the Indexer when no library-specific view model is available
    val showIndexerState = remember { MutableStateFlow(false) }
    val selectedIndexState = remember { MutableStateFlow(0) }
    val activeLettersState = remember { MutableStateFlow<Set<Char>>(emptySet()) }

    // Coroutine scope for scrolling
    val coroutineScope = rememberCoroutineScope()

    // Update indexer state when libraries data changes
    LaunchedEffect(librariesData) {
        // Determine if there is indexable content (movies/tv) across the current libraries data
        val hasIndexableContent = librariesData.any { component ->
            val gridProps = component.props as? NMGridProps
            component.component == "NMGrid" &&
                    gridProps != null &&
                    gridProps.items.any { item ->
                        val cardWrapper = item.props as? NMCardWrapper
                        item.component == "NMCard" &&
                                cardWrapper != null &&
                                (cardWrapper.data?.type in setOf("movie", "tv") )
                    }
        }

        showIndexerState.value = hasIndexableContent

        val indexerCharacters = listOf('#') + ('A'..'Z').toList()

        val activeLetters = if (hasIndexableContent) {
            // If there are movies, enable full indexer set; otherwise compute letters present
            val containsMovie = librariesData.any { component ->
                val gridProps = component.props as? NMGridProps
                component.component == "NMGrid" &&
                        gridProps != null &&
                        gridProps.items.any { item ->
                            val cardWrapper = item.props as? NMCardWrapper
                            item.component == "NMCard" &&
                                    cardWrapper != null &&
                                    (cardWrapper.data?.type == "movie")
                        }
            }

            if (containsMovie) indexerCharacters.toSet()
            else {
                librariesData
                    .filter { it.component == "NMGrid" }
                    .flatMap { (it.props as? NMGridProps)?.items ?: emptyList() }
                    .filter { it.component == "NMCard" }
                    .mapNotNull { (it.props as? NMCardWrapper)?.data?.titleSort ?: (it.props as? NMCardWrapper)?.title }
                    .mapNotNull { sortTitle ->
                        val trimmed = sortTitle.trim()
                        val firstChar = trimmed.firstOrNull { it.isLetterOrDigit() }
                        firstChar?.uppercaseChar()?.let { if (!it.isLetter()) '#' else it }
                    }
                    .toSet()
            }
        } else {
            emptySet()
        }

        activeLettersState.value = activeLetters
    }

    // Track first visible component and update selected index to match
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    LaunchedEffect(firstVisibleItemIndex, librariesData) {
        // Determine which character corresponds to the first visible component
        val gridComponent = librariesData.getOrNull(firstVisibleItemIndex)
        val gridProps = gridComponent?.props as? NMGridProps
        val firstCard = gridProps?.items?.firstOrNull { it.component == "NMCard" }
        val titleSort = (firstCard?.props as? NMCardWrapper)?.data?.titleSort
            ?: (firstCard?.props as? NMCardWrapper)?.title
            ?: ""
        val char = titleSort.firstOrNull()?.uppercaseChar() ?: '#'

        val index = (listOf('#') + ('A'..'Z').toList()).indexOf(char)
        if (index != -1) selectedIndexState.value = index
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

            Indexer(
                modifier = Modifier.align(Alignment.CenterEnd),
                showIndexerState = showIndexerState,
                selectedIndexState = selectedIndexState,
                activeLettersState = activeLettersState,
                onIndexSelectedCallback = { c ->
                    // Scroll the LazyColumn to the first component that contains a card starting with selected char
                    coroutineScope.launch {
                        val targetIndex = librariesData.indexOfFirst { component ->
                            val gridProps = component.props as? NMGridProps
                            component.component == "NMGrid" &&
                                    gridProps != null &&
                                    gridProps.items.any { item ->
                                        val cardWrapper = item.props as? NMCardWrapper
                                        item.component == "NMCard" &&
                                                cardWrapper != null &&
                                                (cardWrapper.data?.title?.startsWith(c, ignoreCase = true) == true
                                                || cardWrapper.title.startsWith(c, ignoreCase = true))
                                    }
                        }
                        if (targetIndex != -1) {
                            listState.animateScrollToItem(targetIndex)
                        }
                    }
                }
            )
        }
    }
}