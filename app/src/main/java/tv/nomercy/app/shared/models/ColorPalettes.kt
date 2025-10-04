package tv.nomercy.app.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class ColorPalettes(
    val poster: PaletteColors? = null,
    val backdrop: PaletteColors? = null,
    val logo: PaletteColors? = null,
    val image: PaletteColors? = null,
    val cover: PaletteColors? = null
)

@Serializable
data class PaletteColors(
    val dominant: String,
    val primary: String,
    val lightVibrant: String,
    val darkVibrant: String,
    val lightMuted: String,
    val darkMuted: String
)