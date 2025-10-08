package tv.nomercy.app.shared.components.nMComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.ComponentData
import tv.nomercy.app.shared.utils.AspectRatio

@Composable
fun <T: ComponentData> NMComponent(
    components: List<Component<out T>>,
    navController: NavController,
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState? = null,
    aspectRatio: AspectRatio? = null,
) {
    components.forEach { component ->
        Box(modifier = modifier.fillMaxWidth()) {
            when (component.component) {
                "NMGrid" -> NMGrid(component, Modifier, navController, lazyGridState)
                "NMCarousel" -> NMCarousel(component, Modifier, navController)
                "NMGenreCard" -> NMGenreCard(component, Modifier, navController)
                "NMHomeCard" -> NMHomeCard(component, Modifier, navController, aspectRatio)
                "NMCard" -> NMCard(component, Modifier, navController, aspectRatio)
                "NMContainer" -> NMContainer(component, Modifier, navController)
                else -> Text(
                    text = "${component.component} is not supported",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}