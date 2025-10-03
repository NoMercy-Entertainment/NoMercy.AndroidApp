
package tv.nomercy.app.lib

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import tv.nomercy.app.api.models.PaletteColors

fun pickPaletteColor(palette: PaletteColors?, dark: Int = 60, light: Int = 160): Color {
    if (palette == null) {
        return Color.Red
    }

    if (!isColorDark(palette.darkVibrant, dark) && !isColorLight(palette.darkVibrant, light)) {
        return Color(palette.darkVibrant.toColorInt())
    }
    if (!isColorDark(palette.lightVibrant, dark) && !isColorLight(palette.lightVibrant, light)) {
        return Color(palette.lightVibrant.toColorInt())
    }
    if (!isColorDark(palette.darkMuted, dark) && !isColorLight(palette.darkMuted, light)) {
        return Color(palette.darkMuted.toColorInt())
    }
    if (!isColorDark(palette.lightMuted, dark) && !isColorLight(palette.lightMuted, light)) {
        return Color(palette.lightMuted.toColorInt())
    }
    if (!isColorDark(palette.dominant, dark) && !isColorLight(palette.dominant, light)) {
        return Color(palette.dominant.toColorInt())
    }

    return Color(palette.primary.toColorInt())
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
