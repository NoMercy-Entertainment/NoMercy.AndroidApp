package tv.nomercy.app.shared.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.nomercy.app.R


@Composable
fun CircularIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRes: Int = R.drawable.exclamationmark,
    iconSize: Dp = 20.dp,
    buttonSize: Dp = 40.dp,
    backgroundColor: Color = Color.Black.copy(alpha = 0.4f),
    iconTint: Color = Color.White,
    contentDescription: String? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.Unspecified // prevent default tint
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            colorFilter = ColorFilter.tint(iconTint)
        )
    }
}