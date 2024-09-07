package pw.binom.dto

import kotlinx.serialization.Serializable

@Serializable
data class PodMetaDataDto(
    val name: String,
    val labels: Map<String, String> = emptyMap(),
    val annotations: Map<String, String> = emptyMap(),
    val namespace: String,
    val uid: String,
)