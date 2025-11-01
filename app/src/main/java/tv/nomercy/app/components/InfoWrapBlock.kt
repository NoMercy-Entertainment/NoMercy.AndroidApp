package tv.nomercy.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoWrapBlock(
    modifier: Modifier = Modifier,
    title: String? = null,
    data: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    ),
    dataStyle: TextStyle = MaterialTheme.typography.bodySmall.copy(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    ),
    headContent: (@Composable RowScope.() -> Unit)? = null,
    itemSpacing: Dp = 6.dp,
    lineSpacing: Dp = 4.dp,
    bodyContent: (@Composable () -> Unit)? = null
) {
    if (data != null || bodyContent != null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            title?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = it.uppercase(),
                        style = titleStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        letterSpacing = 1.3.sp
                    )
                    headContent?.invoke(this)
                }
            }

            Box(
                modifier = modifier,
            ) {
                if (bodyContent != null) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                        verticalArrangement = Arrangement.spacedBy(lineSpacing)
                    ) {
                        bodyContent()
                    }
                } else {
                    Text(
                        text = data ?: "",
                        style = dataStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}