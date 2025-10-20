package tv.nomercy.app.components

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LinkButton(
    modifier: Modifier = Modifier,
    text: Int,
    icon: Int,
    onClick: () -> Unit,
    type: String = "outline",
) {
    val scope = rememberCoroutineScope()
    val isFocused = remember { mutableStateOf(false) }

    val focusColor = MaterialTheme.colorScheme.primary
    val borderColor by animateColorAsState(
        if (isFocused.value) focusColor else when (type) {
            "solid" -> Color.Transparent
            "outline" -> Color.White
            else -> Color.White
        }
    )

    val backgroundColor by animateColorAsState(
        when (type) {
            "solid" -> if (isFocused.value) focusColor else Color.White
            "outline" -> if (isFocused.value) focusColor.copy(alpha = 0.1f) else Color.Transparent
            else -> Color.White
        }
    )

    val textColor by animateColorAsState(
        when (type) {
            else -> Color.White
        }
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .onFocusChanged { isFocused.value = it.isFocused }
            .focusable()
            .padding(vertical = 2.dp)
            .border(
                width = if (type == "outline" || isFocused.value) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .onPreviewKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            scope.launch {
                                onClick()
                            }
                            true
                        }
                        else -> false
                    }
                } else false
            },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxSize()
        )
        {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(textColor)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(text),
                textAlign = TextAlign.Center,
                color = textColor,
                lineHeight = 12.dp.value.sp,
            )
        }
    }
}