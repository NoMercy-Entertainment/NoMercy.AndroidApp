package tv.nomercy.app.shared.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.nomercy.app.shared.components.nMComponents.genreStyle

@Composable
fun GenrePill(
    title: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
    iconSize: Dp = 16.dp,
    pillHeight: Dp = 24.dp
) {
    val style = genreStyle(title)

    Row(
        modifier = modifier
            .height(pillHeight)
            .background(style.backgroundColor, shape = RoundedCornerShape(100))
            .padding(start = 6.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Image(
            painter = painterResource(id = style.iconRes),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            colorFilter = ColorFilter.tint(style.textColor)
        )

        Text(
            text = title,
            style = textStyle,
            color = style.textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}