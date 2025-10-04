package tv.nomercy.app.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class Component<out T>(
    val id: String,
    val component: String,
    val props: Props<T>,
    val update: Update? = null,
    val replacing: String? = null
)