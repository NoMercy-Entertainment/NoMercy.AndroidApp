package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("NMTrackRow")
data class NMTrackRowWrapper(
    val id: String,
    val title: String,
    val data: NMTrackRowProps? = null,
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
    val displayList: List<NMTrackRowProps> = emptyList()
) : ComponentData

@Serializable
@SerialName("NMTrackRow")
data class NMTrackRowProps(
    val id: String,
    val name: String,
    val cover: String? = null,
    val path: String,
    val link: String,
    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null,
    val date: String? = null,
    val disc: Int? = null,
    val duration: String? = null,
    val favorite: Boolean,
    val quality: Int? = null,
    val track: Int? = null,
    val type: String,
    val lyrics: List<Lyric>? = null,
    @SerialName( "album_id")
    val albumId: String,
    @SerialName("album_name")
    val albumName: String,
    @SerialName("artist_track")
    val albumTrack: List<Album> = emptyList(),
    @SerialName("artist_artist")
    val artistTrack: List<Artist> = emptyList()
) : ComponentData