package tv.nomercy.app.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoBlock(
    modifier: Modifier = Modifier,
    title: String? = null,
    data: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    ),
    dataStyle: TextStyle = MaterialTheme.typography.bodySmall.copy(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    ),
    headContent: (@Composable RowScope.() -> Unit)? = null,
    bodyContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        title?.let { title ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title.uppercase(),
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.CenterVertically),
                    letterSpacing = 1.3.sp,
                )
                headContent?.invoke(this)
            }
        }

        Box(
            modifier = Modifier
                .height(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(6.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                )
                .wrapContentWidth()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            when {
                bodyContent != null -> bodyContent()
                data != null -> Text(
                    text = data,
                    style = dataStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                else -> ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        }
    }
}