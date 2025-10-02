
package tv.nomercy.app.lib

import androidx.compose.ui.graphics.Color

fun pickPaletteColor(palette: Map<String, String>?): String? {
    // This is a placeholder. The actual implementation will be added later.
    return palette?.get("lightVibrant")
}

fun parseRgbColor(rgbString: String?): Color? {
    if (rgbString == null || !rgbString.startsWith("rgb(")) return null
    return try {
        val rgb = rgbString.substringAfter("rgb(").substringBefore(")").split(',').map { it.trim().toInt() }
        Color(red = rgb[0], green = rgb[1], blue = rgb[2])
    } catch (e: Exception) {
        null
    }
}
