package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("NMMusicHomeCard")
data class NMMusicHomeCardWrapper(
    val id: String,
    @SerialName("next_id")
    val nextId: String? = null,
    @SerialName("previous_id")
    val previousId: String? = null,
    @SerialName("more_link")
    val moreLink: String? = null,
    @SerialName("more_link_text")
    val moreLinkText: String? = null,
    val watch: Boolean = false,
    val data: NMMusicHomeCardProps,
    @SerialName("context_menu_items")
    val contextMenuItems: List<ContextMenuItem> = emptyList(),
    val url: String? = null,
): ComponentData

@Serializable
@SerialName("NMMusicHomeCard")
data class NMMusicHomeCardProps (
    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null,
    val cover: String? = null,
    val disambiguation: String? = null,
    val description: String? = null,
    val favorite: Boolean? = null,
    val folder: String? = null,
    val id: String,
    val libraryID: String? = null,
    val name: String,
    val trackID: String? = null,
    val type: String? = null,
    val link: String,
    val tracks: Long? = null,
    val year: Int? = null,
): ComponentData
