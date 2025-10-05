package tv.nomercy.app.tv.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.nomercy.app.mobile.screens.auth.AuthViewModel
import tv.nomercy.app.mobile.screens.auth.AuthViewModelFactory
import tv.nomercy.app.shared.stores.GlobalStores


@Composable
fun TVMainScreen() {
    GlobalStores.getServerConfigStore(LocalContext.current)
    GlobalStores.getLibraryStore(LocalContext.current)
    GlobalStores.getAppConfigStore(LocalContext.current)
    viewModel(factory = AuthViewModelFactory(LocalContext.current))
    TODO("Not yet implemented")
}