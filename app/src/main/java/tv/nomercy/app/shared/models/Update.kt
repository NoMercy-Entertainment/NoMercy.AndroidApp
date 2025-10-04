package tv.nomercy.app.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class Update(
    val when_: String? = null,
    val link: String? = null,
    val body: Map<String, String>? = null
)