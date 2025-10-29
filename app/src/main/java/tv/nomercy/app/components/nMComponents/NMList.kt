package tv.nomercy.app.components.nMComponents

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tv.nomercy.app.shared.models.Component
import tv.nomercy.app.shared.models.NMListWrapper
import tv.nomercy.app.shared.utils.assertBoundedWidth

@Composable
fun NMList(
    component: Component,
    modifier: Modifier,
    navController: NavHostController,
) {
    val props = component.props as? NMListWrapper ?: return

    val spacing = 16.dp

    val scrollState = rememberScrollState()

    LaunchedEffect(props.id) {
        scrollState.scrollTo(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .scrollable(scrollState, orientation = Orientation.Vertical)
                .border(1.dp, Color.White)
                .assertBoundedWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            props.items.forEach { item ->
                NMComponent(
                    components = listOf(item),
                    navController = navController,
                )
            }
        }
    }
}