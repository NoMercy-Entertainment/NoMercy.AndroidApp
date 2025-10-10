package tv.nomercy.app.shared.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.nomercy.app.R

@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    text: String? = null,
    minimizedMaxLines: Int = 3,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }

    if (text == null) {
        ShimmerParagraph(
            modifier = modifier,
            lineCount = minimizedMaxLines
        )
        return
    }

    Column(modifier = modifier) {
        Text(
            text = text,
            style = textStyle,
            maxLines = if (isExpanded) Int.MAX_VALUE else minimizedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                isOverflowing = result.hasVisualOverflow
            }
        )

        if (isOverflowing || isExpanded) {
            Text(
                text = if (isExpanded) stringResource(R.string.read_less) else stringResource(R.string.read_more),
                style = textStyle.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { isExpanded = !isExpanded }
            )
        }
    }
}