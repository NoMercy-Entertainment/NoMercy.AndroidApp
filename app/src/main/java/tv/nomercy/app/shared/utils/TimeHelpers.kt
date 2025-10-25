package tv.nomercy.app.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import tv.nomercy.app.R

@Composable
fun formatDuration(seconds: Int): String {
    val context = LocalContext.current
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60

    val dayLabel = pluralString(context, R.plurals.days, days)
    val hourLabel = pluralString(context, R.plurals.hrs, hours)
    val minuteLabel = pluralString(context, R.plurals.min, minutes)

    return buildString {
        if (days > 0) append("$days $dayLabel")
        if (hours > 0) {
            if (isNotEmpty()) append(" ")
            append("$hours $hourLabel")
        }
        if (minutes > 0) {
            if (isNotEmpty()) append(" ")
            append("$minutes $minuteLabel")
        }
    }
}