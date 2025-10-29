package tv.nomercy.app.shared.models

import kotlinx.serialization.KSerializer
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
)


@Serializable
data class ContextMenuItem(
    val id: String? = null,
    val title: String? = null,
    val action: String? = null,
    val icon: String? = null,
    val destructive: Boolean = false
)

// Custom serializer remains the same
object ComponentSerializer : KSerializer<Component> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Component") {
        element<String>("id")
        element<String>("component")
        element<JsonElement>("props")
        element<JsonElement?>("update")
    }

    override fun deserialize(decoder: Decoder): Component {
        require(decoder is JsonDecoder)
        val jsonElement = decoder.decodeJsonElement().jsonObject

        val id = jsonElement["id"]!!.jsonPrimitive.content
        val componentType = jsonElement["component"]!!.jsonPrimitive.content
        val updateJson = jsonElement["update"]

        // Deserialize props based on component type
        val props = when (componentType) {
            "NMGrid" -> decoder.json.decodeFromJsonElement<NMGridWrapper>(jsonElement["props"]!!)
            "NMList" -> decoder.json.decodeFromJsonElement<NMListWrapper>(jsonElement["props"]!!)
            "NMContainer" -> decoder.json.decodeFromJsonElement<NMContainerWrapper>(jsonElement["props"]!!)
            "NMCarousel" -> decoder.json.decodeFromJsonElement<NMCarouselWrapper>(jsonElement["props"]!!)
            "NMMusicHomeCard" -> decoder.json.decodeFromJsonElement<NMMusicHomeCardWrapper>(jsonElement["props"]!!)
            "NMCard" -> decoder.json.decodeFromJsonElement<NMCardWrapper>(jsonElement["props"]!!)
            "NMTrackRow" -> decoder.json.decodeFromJsonElement<NMTrackRowWrapper>(jsonElement["props"]!!)
            "NMHomeCard" -> decoder.json.decodeFromJsonElement<NMHomeCardWrapper>(jsonElement["props"]!!)
            "NMGenreCard" -> decoder.json.decodeFromJsonElement<NMCardWrapper>(jsonElement["props"]!!)
            "NMMusicCard" -> decoder.json.decodeFromJsonElement<NMMusicCardWrapper>(jsonElement["props"]!!)
            "NMTopResultCard" -> decoder.json.decodeFromJsonElement<NMTopResultCardWrapper>(jsonElement["props"]!!)
            else -> throw IllegalArgumentException("Unknown component type: $componentType")
        }

        val update = updateJson?.let { decoder.json.decodeFromJsonElement<Update>(it) }

        return Component(id, componentType, props, update)
    }

    override fun serialize(encoder: Encoder, value: Component) {
        require(encoder is JsonEncoder)
        val jsonObject = buildJsonObject {
            put("id", value.id)
            put("component", value.component)
            put("props", encoder.json.encodeToJsonElement<ComponentData>(value.props))
            value.update?.let { put("update", encoder.json.encodeToJsonElement<Update>(it)) }
        }
        encoder.encodeJsonElement(jsonObject)
    }
}