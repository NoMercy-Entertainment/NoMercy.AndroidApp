package tv.nomercy.app.components.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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

@Composable
fun MediaLikeButton(
    favorite: Boolean?,
    color: Color,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() } // ⛔️ Stop propagation
                    }
                }
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = if (favorite == true) R.drawable.heartfilled else R.drawable.heart),
            contentDescription = if (favorite == true) "Unfavorite" else "Favorite",
            tint = if (favorite == true) color else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
        )
    }
}