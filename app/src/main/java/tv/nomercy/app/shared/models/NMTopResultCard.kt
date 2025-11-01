package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("NMTopResultCard")
data class NMTopResultCardWrapper(
    val id: String,
    val title: String,
    val data: NMTopResultCardProps? = null,
    @SerialName("next_id")
    val nextId: String? = null,
    @SerialName("previous_id")
    val previousId: String? = null,
    @SerialName("more_link")
    val moreLink: String? = null,
    @SerialName("more_link_text")
    val moreLinkText: String? = null,
    val watch: Boolean = false,
    @SerialName("context_menu_items")
    val contextMenuItems: List<ContextMenuItem> = emptyList(),
    val url: String? = null,
): ComponentData

@Serializable
@SerialName("NMTopResultCard")
data class NMTopResultCardProps (
    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null,
    val cover: String? = null,
    val id: String,
    val title: String,
    val type: String,
    val link: String,
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val track: PlaylistItem? = null
): ComponentData
