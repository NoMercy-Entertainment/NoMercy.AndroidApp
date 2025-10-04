interface ComponentData

//package tv.nomercy.app.api.models
//
//import kotlinx.serialization.SerialName
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.json.JsonObject
//
//@Serializable
//data class Update(
//    @SerialName("when") val `when`: String? = null, // "pageLoad", "online", etc.
//    val link: String? = null,
//    val body: Body? = null
//)
//
//@Serializable
//data class Body(
//    @SerialName("replace_id") val replaceId: String,
//    val additionalFields: JsonObject = JsonObject(emptyMap())
//)
//
//@Serializable
//data class Component<out T> (
//    val id: String,
//    val component: String,
//    val props: Props<T>,
//    val update: Update? = null
//)
//
//@Serializable
//data class Props<out T> (
//    val id: String? = null,
//    val title: String,
//    @SerialName("more_link") val moreLink: String? = null,
//    val children: List= emptyList(),
//    val items: List<Component<T>> = emptyList(),
//    val data: T? = null
//)
