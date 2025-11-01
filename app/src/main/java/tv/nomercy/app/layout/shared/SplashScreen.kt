package tv.nomercy.app.shared.layout

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import tv.nomercy.app.R
import tv.nomercy.app.shared.ui.SystemUiController

data class SplashTheme(
    val backgroundStartColor: Color,
    val backgroundEndColor: Color,
    val iconTint: Color
)

@Composable
fun ThemedSplashScreen() {
    val activity = LocalActivity.current

    DisposableEffect(Unit) {
        val controller = SystemUiController
        controller.hideStatusBar(activity!!)
        controller.hideNavigationBar(activity)

        onDispose {
            controller.showStatusBar(activity)
            controller.showNavigationBar(activity)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SplashBackground()
        SplashIcon()
    }
}

@Composable
fun SplashBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // base fill
    ) {

        val colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        )

        val gradient = Brush.verticalGradient(
            colors = colors,
            startY = 0f,
            endY = 2000f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()// triggers layer
                .blur(80.dp)
                .background(gradient)
        )

    }
}

@Composable
fun SplashIcon() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(52.dp),
        contentAlignment = Alignment.Center
    ) {

        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.splash_logo_embossed_dark)
                .build()
        )

        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
