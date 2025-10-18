package tv.nomercy.app.views.auth.tv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tv.nomercy.app.shared.utils.generateQrBitmap
import kotlin.math.max

@Composable
fun TvLoginInstructions(
    verificationUri: String,
    userCode: String,
    expiresAt: Long,
    onConfirm: () -> Unit
) {
    // Start polling automatically when the instructions screen appears for this userCode
    LaunchedEffect(userCode) {
        onConfirm()
    }

    // Countdown state in milliseconds
    var remainingMs by remember(userCode, expiresAt) { mutableStateOf(max(0L, expiresAt - System.currentTimeMillis())) }

    LaunchedEffect(expiresAt, userCode) {
        while (true) {
            val now = System.currentTimeMillis()
            val newRem = max(0L, expiresAt - now)
            remainingMs = newRem
            if (newRem <= 0L) break
            delay(1000L)
        }
    }

    val qrBitmap = remember(userCode) {
        val url = "https://dev.nomercy.tv/tv?code=$userCode"
        generateQrBitmap(url, size = 1024)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(52.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Instructions
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Follow these steps on your computer or mobile device:",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "1. Go to $verificationUri\n2. Enter the code below\n3. Your TV will be ready to watch",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = userCode,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // Show countdown or refreshing indicator
            val minutes = (remainingMs / 1000L) / 60L
            val seconds = (remainingMs / 1000L) % 60L
            val countdownText = if (remainingMs > 0L) {
                String.format("Code expires in %d:%02d", minutes, seconds)
            } else {
                "Refreshing code..."
            }

            Text(
                text = countdownText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Right: QR Code
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .background(Color.White, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .fillMaxSize(0.95f)
                    .padding(8.dp)
            )
        }
    }
}