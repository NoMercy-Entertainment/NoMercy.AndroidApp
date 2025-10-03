package tv.nomercy.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.nomercy.app.api.models.Component

@Composable
fun <T> NMComponent(
    components: List<Component<T>>,
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        components.forEach { component ->
            when (component.component) {
                "NMGrid" -> {
                    NMGrid(
                        component = component,
                        navController = navController,
                        modifier = modifier
                    )
                }

                "NMCarousel" -> {
                    NMCarousel(
                        component = component,
                        navController = navController,
                        modifier = modifier
                    )
                }

                "NMCard" -> {
                    NMCard(
                        component = component,
                        navController = navController,
                        modifier = modifier
                    )
                }

                else -> {
                    Text(
                        text = component.props.items.joinToString { it -> it.component + ", " },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}