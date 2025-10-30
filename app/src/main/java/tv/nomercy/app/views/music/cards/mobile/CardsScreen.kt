package tv.nomercy.app.views.music.cards.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tv.nomercy.app.R
import tv.nomercy.app.components.EmptyGrid
import tv.nomercy.app.components.Indexer
import tv.nomercy.app.components.SetThemeColor
import tv.nomercy.app.components.nMComponents.NMComponent
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.views.music.cards.shared.CardsViewModel
import tv.nomercy.app.views.music.cards.shared.CardsViewModelFactory

@Composable
fun CardsScreen(
    type: String,
    char: Char?,
    navController: NavHostController
) {

    val viewModel: CardsViewModel = viewModel(
        factory = CardsViewModelFactory(
            cardStore = GlobalStores.getCardStore(LocalContext.current),
        )
    )

    val cardsData by viewModel.cards.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // derive empty state from data + loading
    val isEmptyStable = !isLoading && cardsData.isEmpty()

    val lazyGridState = rememberLazyGridState()

    // Select the card set when the screen appears
    LaunchedEffect(type, char) {
        viewModel.selectCard(type, char)
    }

    SetThemeColor()

    Column(
        modifier = Modifier.fillMaxSize(),
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
                        viewModel.clearError()
                        viewModel.refresh()
                    }) {
                        Text(stringResource(R.string.try_again))
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Main content — takes remaining width
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    isEmptyStable -> {
                        EmptyGrid(modifier = Modifier.fillMaxSize(), text = "No content available in this library.")
                    }

                    else -> {
                        NMComponent(
                            components = cardsData,
                            navController = navController,
                            modifier = Modifier.fillMaxSize(),
                            lazyGridState = lazyGridState
                        )
                    }
                }
            }

            Indexer(
                modifier = Modifier,
                showIndexerState = viewModel.showIndexer,
                selectedIndexState = viewModel.selectedIndex,
                activeLettersState = viewModel.activeIndexerLetters,
                onIndexSelectedCallback = { c -> viewModel.onIndexSelected(c) }
            )
         }
     }
 }