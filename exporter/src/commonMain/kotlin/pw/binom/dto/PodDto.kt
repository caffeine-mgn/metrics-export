package pw.binom.dto

import kotlinx.serialization.Serializable

@Serializable
data class PodDto(
    val metadata: PodMetaDataDto,
    val status: PodStatusDto,
) {
}