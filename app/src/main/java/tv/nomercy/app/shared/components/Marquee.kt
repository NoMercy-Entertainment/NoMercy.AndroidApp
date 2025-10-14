package tv.nomercy.app.shared.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun Marquee(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState()),
        content = content
    )
}
