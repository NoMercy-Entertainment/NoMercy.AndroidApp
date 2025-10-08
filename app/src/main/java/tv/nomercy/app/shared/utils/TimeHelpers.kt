package tv.nomercy.app.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import tv.nomercy.app.R

@Composable
fun formatDuration(seconds: Int): String {
    val context = LocalContext.current
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60

    val hourLabel = pluralString(context, R.plurals.hrs, hours)
    val minuteLabel = pluralString(context, R.plurals.min, minutes)

    return buildString {
        if (hours > 0) append("$hours $hourLabel")
        if (minutes > 0) {
            if (isNotEmpty()) append(" ")
            append("$minutes $minuteLabel")
        }
    }
}
