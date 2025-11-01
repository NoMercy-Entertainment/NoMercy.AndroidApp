package tv.nomercy.app.components.nMComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMCarouselWrapper
import tv.nomercy.app.shared.models.NMContainerWrapper
import tv.nomercy.app.shared.models.NMGridWrapper
import tv.nomercy.app.shared.models.NMHomeCardWrapper
import tv.nomercy.app.shared.models.NMListWrapper
import tv.nomercy.app.shared.utils.AspectRatio

@Composable
fun NMComponent(
    components: List<Component>,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState? = null,
    aspectRatio: AspectRatio? = null,
) {
    components
        .filter { hasContent(it) }
        .forEach { component ->
            Box(modifier = Modifier) {
                when (component.component) {
                    "NMCard" -> NMCard(component, modifier, navController, aspectRatio)
                    "NMCarousel" -> NMCarousel(component, modifier, navController)
                    "NMContainer" -> NMContainer(component, modifier, navController)
                    "NMGenreCard" -> NMGenreCard(component, modifier, navController)
                    "NMGrid" -> NMGrid(component, modifier, navController, lazyGridState)
                    "NMHomeCard" -> NMHomeCard(component, modifier, navController, aspectRatio)
                    "NMList" -> NMList(component, modifier, navController)
                    "NMMusicCard" -> NMMusicCard(component, modifier, navController)
                    "NMMusicHomeCard" -> NMMusicHomeCard(component, modifier, navController, aspectRatio)
                    "NMTopResultCard" -> NMTopResultCard(component, modifier, navController)
                    "NMTrackRow" -> NMTrackRow(component, modifier, navController)
                    "" -> {}
                    else -> Text(
                        text = "${component.component} is not supported",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
    }
}

fun hasContent(component: Component): Boolean {
    return when (val props = component.props ) {
        is NMCarouselWrapper -> props.items.isNotEmpty()
        is NMGridWrapper -> props.items.isNotEmpty()
        is NMListWrapper -> props.items.isNotEmpty()
        is NMContainerWrapper -> props.items.isNotEmpty()
        is NMHomeCardWrapper -> props.data != null
        else -> true
    }
}