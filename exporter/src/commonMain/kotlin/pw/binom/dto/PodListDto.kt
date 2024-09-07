package pw.binom.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("PodList")
@Serializable
data class PodListDto(
    val metadata: ListMetadataDto,
    val items: List<PodDto>,
)