package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Props<out T>(
    val id: String? = null,
    val title: String,
    @SerialName("more_link")
    val moreLink: String? = null,
    @SerialName("more_link_text")
    val moreLinkText: String? = null,
    val items: List<Component<T>> = emptyList(),
    val children: List<JsonElement> = emptyList(),
    val data: T? = null,
    val watch: Boolean? = null,
    @SerialName("context_menu_items")
    val contextMenuItems: List<JsonElement> = emptyList(),
    val url: String? = null,
    val displayList: List<JsonElement> = emptyList()
)