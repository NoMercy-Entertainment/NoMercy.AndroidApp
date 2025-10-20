package tv.nomercy.app.shared.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable
sealed interface ComponentData

@Serializable(with = ComponentSerializer::class)
data class Component(
    val id: String,
    val component: String,
    val props: ComponentData,
    val update: Update? = null,
    val replacing: String? = null
)

// Wrapper props for NMCard and NMHomeCard that have nested data
@Serializable
@SerialName("NMCard")
data class NMCardWrapper(
    val id: String,
    val title: String,
    val data: NMCardProps? = null,
    val items: List<Component> = emptyList(),
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
) : ComponentData

@Serializable
@SerialName("NMHomeCard")
data class NMHomeCardWrapper(
    val id: String,
    val title: String,
    val data: NMHomeCardProps? = null,
    val items: List<Component> = emptyList(),
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
) : ComponentData

// Actual card data structure
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

@Serializable
@SerialName("NMCarousel")
data class NMCarouselProps(
    val id: String,
    val title: String? = null,
    val items: List<Component> = emptyList(),
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
    val items: List<Component> = emptyList(),
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
    val items: List<Component> = emptyList(),
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

@Serializable
@SerialName("NMMusicHomeCard")
data class NMMusicHomeCardWrapper(
    val id: String,
    val title: String,
    val data: NMMusicHomeCardProps? = null,
    val items: List<Component> = emptyList(),
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
) : ComponentData


// Custom serializer remains the same
object ComponentSerializer : KSerializer<Component> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Component") {
        element<String>("id")
        element<String>("component")
        element<JsonElement>("props")
        element<JsonElement?>("update")
        element<String?>("replacing")
    }

    override fun deserialize(decoder: Decoder): Component {
        require(decoder is JsonDecoder)
        val jsonElement = decoder.decodeJsonElement().jsonObject

        val id = jsonElement["id"]!!.jsonPrimitive.content
        val componentType = jsonElement["component"]!!.jsonPrimitive.content
        val updateJson = jsonElement["update"]
        val replacing = jsonElement["replacing"]?.jsonPrimitive?.content

        // Deserialize props based on component type
        val props = when (componentType) {
            "NMGrid" -> decoder.json.decodeFromJsonElement<NMGridProps>(jsonElement["props"]!!)
            "NMContainer" -> decoder.json.decodeFromJsonElement<NMContainerProps>(jsonElement["props"]!!)
            "NMCarousel" -> decoder.json.decodeFromJsonElement<NMCarouselProps>(jsonElement["props"]!!)
            "NMMusicHomeCard" -> decoder.json.decodeFromJsonElement<NMMusicHomeCardProps>(jsonElement["props"]!!)
            "NMCard" -> decoder.json.decodeFromJsonElement<NMCardWrapper>(jsonElement["props"]!!)
            "NMHomeCard" -> decoder.json.decodeFromJsonElement<NMHomeCardWrapper>(jsonElement["props"]!!)
            "NMGenreCard" -> decoder.json.decodeFromJsonElement<NMCardWrapper>(jsonElement["props"]!!)
            "NMMusicCard" -> decoder.json.decodeFromJsonElement<NMMusicHomeCardProps>(jsonElement["props"]!!)
            else -> decoder.json.decodeFromJsonElement<NMMusicHomeCardProps>(jsonElement["props"]!!)
        }

        val update = updateJson?.let { decoder.json.decodeFromJsonElement<Update>(it) }

        return Component(id, componentType, props, update, replacing)
    }

    override fun serialize(encoder: Encoder, value: Component) {
        require(encoder is JsonEncoder)
        val jsonObject = buildJsonObject {
            put("id", value.id)
            put("component", value.component)
            put("props", encoder.json.encodeToJsonElement<ComponentData>(value.props))
            value.update?.let { put("update", encoder.json.encodeToJsonElement<Update>(it)) }
            value.replacing?.let { put("replacing", it) }
        }
        encoder.encodeJsonElement(jsonObject)
    }
}