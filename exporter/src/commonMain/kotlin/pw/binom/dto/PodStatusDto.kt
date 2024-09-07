package pw.binom.dto

import kotlinx.serialization.Serializable

@Serializable
data class PodStatusDto(
    val podIP: String,
    val phase: String,
)