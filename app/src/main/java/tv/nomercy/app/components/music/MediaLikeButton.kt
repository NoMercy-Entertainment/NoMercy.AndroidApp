package tv.nomercy.app.components.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import tv.nomercy.app.R
import tv.nomercy.app.components.MoooomIcon
import tv.nomercy.app.components.MoooomIconName

@Composable
fun MediaLikeButton(
    favorite: Boolean?,
    modifier: Modifier = Modifier,
    color: Color,
) {
    IconButton(
        onClick = { },
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() } // ⛔️ Prevent propagation
                    }
                }
            },
    ) {
        MoooomIcon(
            icon = if (favorite == true) MoooomIconName.HeartFilled else MoooomIconName.Heart,
            contentDescription = if (favorite == true) "Unfavorite" else "Favorite",
            tint = if (favorite == true) color else MaterialTheme.colorScheme.onSurface,
        )
    }
}