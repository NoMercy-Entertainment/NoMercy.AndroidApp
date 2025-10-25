package tv.nomercy.app.components

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Rect
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
    type: String = "solid",
) {
    val scope = rememberCoroutineScope()
    val isFocused = remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val focusColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceBright
    val borderColor by animateColorAsState(
        if (isFocused.value) focusColor else when (type) {
            "solid" -> Color.Transparent
            "outline" -> Color.White
            else -> Color.White
        }
    )

    val backgroundColor by animateColorAsState(
        when (type) {
            "solid" -> if (isFocused.value) focusColor else surfaceColor
            "outline" -> if (isFocused.value) focusColor.copy(alpha = 0.1f) else Color.Transparent
            else -> Color.White
        }
    )

    val textColor by animateColorAsState(
        when (type) {
            "solid" -> if (isFocused.value) Color.White.copy(alpha = 0.8f) else Color.White
            else -> Color.White
        }
    )

    Row(
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { isFocused.value = it.isFocused }
            .focusable()
            //            .height(32.dp)
            .clip(CircleShape)
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .then(
                if (type == "outline") {
                    Modifier
                        .border(
                            width = if (isFocused.value) 2.dp else 0.dp,
                            color = borderColor,
                            shape = CircleShape
                        )
                } else {
                    Modifier
                }
            )
            .onFocusEvent {
                if (it.isFocused) {
                    scope.launch {
                        bringIntoViewRequester.bringIntoView(rect = Rect(0f, 0f, 0f, 500000f))
                    }
                }
            }
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
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .padding(vertical = 6.dp, horizontal = 12.dp)
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