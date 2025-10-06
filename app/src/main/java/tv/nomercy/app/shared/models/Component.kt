package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
sealed interface ComponentData

@Serializable
data class Component<T : ComponentData>(
    val id: String,
    val component: String,
    val props: Props<T>,
    val update: Update? = null,
    val replacing: String? = null
)

@Serializable
data class Props<T : ComponentData>(
    val id: String,
    val title: String,
    val data: T? = null,
    val items: List<Component<T>> = emptyList(),
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
    val contextMenuItems: List<JsonElement> = emptyList(),
    val url: String? = null,
    val displayList: List<String> = emptyList()
)

@Serializable
@SerialName("NMCarousel")
data class NMCarouselProps(
    val id: String,
    val title: String? = null,
    val items: List<Component<ComponentData>> = emptyList(),
    @SerialName("next_id")
    val nextId: String? = null,
    @SerialName("previous_id")
    val previousId: String? = null,
    @SerialName("more_link")
    val moreLink: String? = null,
    @SerialName("more_link_text")
    val moreLinkText: String? = null,
    @SerialName("context_menu_items")
    val contextMenuItems: List<ContextMenuItem> = emptyList(),
    val url: String? = null,
    val displayList: List<String> = emptyList()
) : ComponentData

@Serializable
@SerialName("NMGrid")
data class NMGridProps(
    val id: String,
    val title: String? = null,
    val items: List<Component<ComponentData>> = emptyList(),
    @SerialName("next_id")
    val nextId: String? = null,
    @SerialName("previous_id")
    val previousId: String? = null,
    @SerialName("more_link")
    val moreLink: String? = null,
    @SerialName("more_link_text")
    val moreLinkText: String? = null,
    @SerialName("context_menu_items")
    val contextMenuItems: List<ContextMenuItem> = emptyList(),
    val url: String? = null,
    val displayList: List<String> = emptyList()
) : ComponentData

@Serializable
@SerialName("NMContainer")
data class NMContainerProps(
    val id: String,
    val title: String? = null,
    val items: List<Component<ComponentData>> = emptyList(),
    @SerialName("next_id")
    val nextId: String? = null,
    @SerialName("previous_id")
    val previousId: String? = null,
    @SerialName("more_link")
    val moreLink: String? = null,
    @SerialName("more_link_text")
    val moreLinkText: String? = null,
    @SerialName("context_menu_items")
    val contextMenuItems: List<ContextMenuItem> = emptyList(),
    val url: String? = null,
    val displayList: List<String> = emptyList()
) : ComponentData

@Serializable
data class ContextMenuItem(
    val id: String,
    val title: String,
    val action: String,
    val icon: String? = null,
    val destructive: Boolean = false
)
