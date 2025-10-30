package tv.nomercy.app.views.base.home.mobile

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tv.nomercy.app.R
import tv.nomercy.app.components.EmptyGrid
import tv.nomercy.app.components.SetThemeColor
import tv.nomercy.app.components.nMComponents.NMComponent
import tv.nomercy.app.shared.models.NMHomeCardWrapper
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.LocalThemeOverrideManager
import tv.nomercy.app.shared.utils.pickPaletteColor
import tv.nomercy.app.views.base.home.shared.HomeViewModel
import tv.nomercy.app.views.base.home.shared.HomeViewModelFactory
import java.util.UUID

@Composable
fun MobileHomeScreen(
    navController: NavHostController,
) {
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

    val posterPalette = homeData.firstOrNull()?.props.let {
        when (it) {
            is NMHomeCardWrapper -> it.data?.colorPalette?.poster
            else -> null
        }
    }
    
    val systemAppConfigStore = GlobalStores.getAppConfigStore(context)
    val useAutoThemeColors by systemAppConfigStore.useAutoThemeColors.collectAsState()

    val fallbackColor = MaterialTheme.colorScheme.primary
    val focusColor: Color = remember(posterPalette) {
        if (!useAutoThemeColors) fallbackColor
        else pickPaletteColor(posterPalette) ?:fallbackColor
    }

    SetThemeColor(color = focusColor)

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
            when {
                homeData.isNotEmpty() -> {

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(homeData, key = { it.id }) { component ->
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
                    // Only show loading if we don't have data
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
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
