package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
@SerialName("NMCard")
data class NMCardWrapper(
    val id: String,
    val title: String,
    val data: NMCardProps? = null,
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
) : ComponentData

@Serializable
@SerialName("NMCard")
data class NMCardProps(
    val id: String,
    val title: String,
    val titleSort: String,
    val overview: String? = null,
    val link: String,
    val rating: Double? = null,
    val year: Int? = null,
    val duration: Int? = null,
    val type: String,
    val backdrop: String? = null,
    val poster: String? = null,
    val logo: String? = null,
    @SerialName(value = "color_palette")
    val colorPalette: ColorPalettes? = null,
    @SerialName("content_ratings")
    val contentRatings: List<JsonElement> = emptyList(),
    @SerialName("have_items")
    val haveItems: Int? = null,
    @SerialName("number_of_items")
    val numberOfItems: Int? = null
) : ComponentData