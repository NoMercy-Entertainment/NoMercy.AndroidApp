package tv.nomercy.app.shared.components.NMComponents

import ComponentData
import HomeItem
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.MediaItem

@Composable
fun <T: ComponentData> NMComponent(
    components: List<Component<T>>,
    navController: NavController,
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState? = null,
) {
    components.forEach { component ->
        when (component.component) {
            "NMGrid" -> NMGrid(component, modifier, navController, lazyGridState)
            "NMCarousel" -> NMCarousel(component, modifier, navController)
            "NMCard" -> {
                when (component.props.data) {
                    is HomeItem -> NMCard(component, modifier, navController)
                    is MediaItem -> NMCard(component, modifier, navController)
                    else -> Text(
                        text = "NMCard received unsupported data type: ${component.props.data?.javaClass?.simpleName}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> Text(
                text = "${component.component} is not supported",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}