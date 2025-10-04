package tv.nomercy.app.shared.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import tv.nomercy.app.shared.models.PaletteColors

fun pickPaletteColor(palette: PaletteColors?, dark: Int = 60, light: Int = 160): Color {
    if (palette == null) {
        return Color.Transparent
    }

    if (!isColorLight(palette.lightVibrant, light) && !isColorDark(palette.lightVibrant, dark)) {
        return palette.lightVibrant.toColor()
    }
    if (!isColorLight(palette.primary, light) && !isColorDark(palette.primary, dark)) {
        return palette.primary.toColor()
    }
    if (!isColorLight(palette.dominant, light) && !isColorDark(palette.dominant, dark)) {
        return palette.dominant.toColor()
    }
    if (!isColorLight(palette.darkVibrant, light) && !isColorDark(palette.darkVibrant, dark)) {
        return palette.darkVibrant.toColor()
    }
    if (!isColorLight(palette.darkMuted, light) && !isColorDark(palette.darkMuted, dark)) {
        return palette.darkMuted.toColor()
    }
    if (!isColorLight(palette.lightMuted, light) && !isColorDark(palette.lightMuted, dark)) {
        return palette.lightMuted.toColor()
    }

    return palette.primary.toColor()
}

fun String.toColor(): Color {
    val cleaned = this.removePrefix("#")
    val argb = when (cleaned.length) {
        8 -> "#${cleaned.substring(6, 8)}${cleaned.substring(0, 6)}" // RGBA → ARGB
        6 -> "#$cleaned" // RGB
        else -> "#FF0000" // fallback to red
    }
    return try {
        Color(android.graphics.Color.parseColor(argb))
    } catch (e: IllegalArgumentException) {
        Color.Red
    }
}

fun getPerceivedBrightness(color: String): Int {
    val rgb = hexToRgb(color)
    val r = rgb["red"]!!.toInt()
    val g = rgb["green"]!!.toInt()
    val b = rgb["blue"]!!.toInt()
    return ((r * 299) + (g * 587) + (b * 114)) / 1000
}

fun isColorDark(color: String, minBrightness: Int = 50): Boolean {
    return getPerceivedBrightness(color) < minBrightness
}
fun isColorLight(color: String, maxBrightness: Int = 130): Boolean {
    return getPerceivedBrightness(color) >= maxBrightness
}
fun hexToRgb(color: String): Map<String, String> {
    val c = color.toColorInt()

    return mapOf(
        "red" to ((c shr 16) and 0xFF).toString(),
        "green" to ((c shr 8) and 0xFF).toString(),
        "blue" to (c and 0xFF).toString()
    )
}
