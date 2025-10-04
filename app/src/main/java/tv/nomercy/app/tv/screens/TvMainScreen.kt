package tv.nomercy.app.tv.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvMainScreen() {
    Scaffold(
        topBar = {
            // TV apps often have a different style for the top bar or a side navigation.
            // For now, a simple TopAppBar.
            TopAppBar(title = { Text("NoMercy TV") })
        }
        // Unlike mobile, bottom navigation is not common on TV.
        // Navigation is usually handled by a side panel or within the content area.
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for TV content and navigation elements
            Text("TV Screen Content Placeholder")
            // We'll later integrate D-pad focus management and TV-specific navigation components here.
        }
    }
}

// The `device` parameter in @Preview helps visualize it on a TV screen.
@Preview(showBackground = true, device = "id:tv_1080p")
@Composable
fun DefaultPreviewTvMainScreen() {
    TvMainScreen()
}
