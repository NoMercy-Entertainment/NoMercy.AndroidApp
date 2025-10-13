package tv.nomercy.app.shared.components.nMComponents

import NMGrid
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.utils.AspectRatio

@Composable
fun NMComponent(
    components: List<Component>,
    navController: NavController,
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState? = null,
    aspectRatio: AspectRatio? = null,
) {
    components.forEach { component ->
        // Use the passed-in modifier directly so child scrollables receive proper constraints
        Box(modifier = modifier) {
            when (component.component) {
                "NMGrid" -> NMGrid(component, modifier, navController, lazyGridState)
                "NMCarousel" -> NMCarousel(component, modifier, navController)
                "NMGenreCard" -> NMGenreCard(component, modifier, navController)
                "NMHomeCard" -> NMHomeCard(component, modifier, navController, aspectRatio)
                "NMCard" -> NMCard(component, modifier, navController, aspectRatio)
                "NMContainer" -> NMContainer(component, modifier, navController)
                "NMMusicCard" -> NMMusicCard(component, modifier, navController, aspectRatio)
                "NMMusicHomeCard" -> NMMusicHomeCard(component, modifier, navController, aspectRatio)
                "" -> {}
                else -> Text(
                    text = "${component.component} is not supported",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}