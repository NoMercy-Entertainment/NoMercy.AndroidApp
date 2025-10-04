package tv.nomercy.app.shared.models

import ComponentData
import HomeItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

//fun <T> Component<T>.resolveData(json: Json): ComponentData? {
//    return when (component) {
//        "NMHomeCard" -> props.data?.let { json.decodeFromJsonElement<HomeItem>(it) }
//        "NMCard" -> props.data?.let { json.decodeFromJsonElement<MediaItem>(it) }
//        "NMCarousel" -> props.data?.let { json.decodeFromJsonElement<MediaItem>(it) }
//        "NMGrid" -> props.data?.let { json.decodeFromJsonElement<MediaItem>(it) }
//        else -> null
//    }
//}
fun Component<JsonElement>.resolveData(json: Json): ComponentData? {
    return when (component) {
        "NMHomeCard" -> props.data?.let { json.decodeFromJsonElement<HomeItem>(it) }
        "NMCard" -> props.data?.let { json.decodeFromJsonElement<MediaItem>(it) }
        "NMCarousel" -> props.data?.let { json.decodeFromJsonElement<MediaItem>(it) }
        "NMGrid" -> props.data?.let { json.decodeFromJsonElement<MediaItem>(it) }
        else -> null
    }
}

fun JsonElement.toComponentData(json: Json): ComponentData? {
    return when (this.jsonObject["component"]?.toString()?.replace("\"", "")) {
        "NMHomeCard" -> json.decodeFromJsonElement<HomeItem>(this)
        "NMCard" -> json.decodeFromJsonElement<MediaItem>(this)
        "NMCarousel" -> json.decodeFromJsonElement<MediaItem>(this)
        "NMGrid" -> json.decodeFromJsonElement<MediaItem>(this)
        else -> null
    }
}
