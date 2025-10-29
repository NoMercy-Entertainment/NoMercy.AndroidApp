package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
@SerialName("NMHomeCard")
data class NMHomeCardWrapper(
    val id: String,
    val title: String,
    val data: NMHomeCardProps? = null,
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
@SerialName("NMHomeCard")
data class NMHomeCardProps(

    val id: FlexibleId,
    val title: String,
    val overview: String? = null,
    val link: String,
    val rating: String? = null,
    val year: Int? = null,
    val duration: Int? = null,
    val backdrop: String? = null,
    val poster: String? = null,
    val logo: String? = null,

    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null,

    @SerialName("content_ratings")
    val contentRatings: List<Rating> = emptyList(),

    @SerialName("have_items")
    val haveItems: Int? = null,

    @SerialName("number_of_items")
    val numberOfItems: Int? = null,

    @SerialName("media_type")
    val mediaType: String? = null,

    val videos: List<ExtendedVideo> = emptyList(),
    val videoID: String? = null,
): ComponentData