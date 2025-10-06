package tv.nomercy.app.shared.components.nMComponents

import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.ComponentData

@Composable
fun <T: ComponentData> NMContainer(
    component: Component<out T>,
    modifier: Modifier,
    navController: NavController,
) {

    Text(component.component)

    NMComponent(
        components = component.props.items,
        navController = navController,
        modifier = modifier.border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant),
    )

}