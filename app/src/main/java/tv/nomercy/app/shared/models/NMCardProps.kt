package tv.nomercy.app.shared.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int

@Serializable(with = FlexibleIdSerializer::class)
sealed class FlexibleId {
    data class IntId(val value: Int) : FlexibleId()
    data class StringId(val value: String) : FlexibleId()

    override fun toString(): String = when (this) {
        is IntId -> value.toString()
        is StringId -> value
    }
}

object FlexibleIdSerializer : KSerializer<FlexibleId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FlexibleId) {
        when (value) {
            is FlexibleId.IntId -> encoder.encodeInt(value.value)
            is FlexibleId.StringId -> encoder.encodeString(value.value)
        }
    }

    override fun deserialize(decoder: Decoder): FlexibleId {
        val input = decoder as? JsonDecoder ?: error("FlexibleId can only be deserialized from JSON")
        val element = input.decodeJsonElement()

        return when (element) {
            is JsonPrimitive -> {
                if (element.isString) {
                    FlexibleId.StringId(element.content)
                } else {
                    FlexibleId.IntId(element.int)
                }
            }
            else -> error("Invalid FlexibleId format")
        }
    }
}