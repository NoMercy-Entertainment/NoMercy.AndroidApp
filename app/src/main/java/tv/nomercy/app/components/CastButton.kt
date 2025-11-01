package tv.nomercy.app.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CastButton(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            androidx.mediarouter.app.MediaRouteButton(context).apply {
                com.google.android.gms.cast.framework.CastButtonFactory.setUpMediaRouteButton(
                    context,
                    this
                )
            }
        }
    )
}