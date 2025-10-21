package tv.nomercy.app.shared.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import tv.nomercy.app.shared.models.PaletteColors

fun pickPaletteColor(palette: PaletteColors?, dark: Int = 60, light: Int = 160, fallbackColor: Color?): Color {
    if (palette == null) {
        return fallbackColor ?: Color.Transparent
    }

    if (palette.lightVibrant != null && !isColorLight(palette.lightVibrant, light) && !isColorDark(palette.lightVibrant, dark)) {
        return palette.lightVibrant.toColor()
    }
    if (palette.primary != null && !isColorLight(palette.primary, light) && !isColorDark(palette.primary, dark)) {
        return palette.primary.toColor()
    }
    if (palette.dominant != null && !isColorLight(palette.dominant, light) && !isColorDark(palette.dominant, dark)) {
        return palette.dominant.toColor()
    }
    if (palette.darkVibrant != null && !isColorLight(palette.darkVibrant, light) && !isColorDark(palette.darkVibrant, dark)) {
        return palette.darkVibrant.toColor()
    }
    if (palette.darkMuted != null && !isColorLight(palette.darkMuted, light) && !isColorDark(palette.darkMuted, dark)) {
        return palette.darkMuted.toColor()
    }
    if (palette.lightMuted != null && !isColorLight(palette.lightMuted, light) && !isColorDark(palette.lightMuted, dark)) {
        return palette.lightMuted.toColor()
    }

    return fallbackColor ?: Color.Transparent
}

fun String.toColor(): Color {
    val cleaned = this.removePrefix("#")
    val argb = when (cleaned.length) {
        8 -> "#${cleaned.substring(6, 8)}${cleaned.substring(0, 6)}" // RGBA → ARGB
        6 -> "#$cleaned" // RGB
        else -> "#FF0000" // fallback to red
    }
    return try {
        Color(argb.toColorInt())
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

fun getPerceivedBrightness(color: Color): Int {
    return (((color.red * 255 * 299) + (color.green * 255 * 587) + (color.blue * 255 * 114)) / 1000).toInt()
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

data class PercentColor(val pct: Int, val color: Color)

val redToGreen = listOf(
    PercentColor(0, Color(0xFFCC0000)),     // red
    PercentColor(50, Color(0xFFEEEE00)),    // yellow
    PercentColor(99, Color(0xFF009000)),    // green
    PercentColor(100, Color(0xFF004000))    // dark green
)

fun getColorFromPercent(pct: Int, scheme: List<PercentColor> = redToGreen): Color {
    val clampedPct = pct.coerceIn(0, 100)
    val i = scheme.indexOfFirst { clampedPct < it.pct }.takeIf { it > 0 } ?: scheme.lastIndex
    val lower = scheme[i - 1]
    val upper = scheme[i]

    val range = (upper.pct - lower.pct).coerceAtLeast(1)
    val rangePct = (clampedPct - lower.pct).toFloat() / range
    val pctLower = 1f - rangePct
    val pctUpper = rangePct

    val r = (lower.color.red * pctLower + upper.color.red * pctUpper)
    val g = (lower.color.green * pctLower + upper.color.green * pctUpper)
    val b = (lower.color.blue * pctLower + upper.color.blue * pctUpper)

    return Color(r, g, b)
}

fun isColorLight(color: Color): Boolean {
    val lightness  = getPerceivedBrightness(color)
    return lightness >= 130
}