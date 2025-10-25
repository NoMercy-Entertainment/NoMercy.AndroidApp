package tv.nomercy.app.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class ColorPalettes(
    val poster: PaletteColors? = null,
    val backdrop: PaletteColors? = null,
    val logo: PaletteColors? = null,
    val image: PaletteColors? = null,
    val cover: PaletteColors? = null,
    val still: PaletteColors? = null,
    val profile: PaletteColors? = null,
)

@Serializable
data class PaletteColors(
    val dominant: String? = null,
    val primary: String? = null,
    val lightVibrant: String? = null,
    val darkVibrant: String? = null,
    val lightMuted: String? = null,
    val darkMuted: String? = null,
)