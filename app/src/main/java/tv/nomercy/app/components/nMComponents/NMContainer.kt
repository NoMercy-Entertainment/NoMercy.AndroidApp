package tv.nomercy.app.components.nMComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMContainerWrapper

@Composable
fun NMContainer(
    component: Component,
    modifier: Modifier,
    navController: NavHostController,
) {
    val props = component.props as? NMContainerWrapper ?: return

    val spacing = 16.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        props.items.forEachIndexed { index, item ->
            NMComponent(
                components = listOf(item),
                navController = navController
            )
        }
    }
}