package pw.binom.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ListMetadataDto(
    val resourceVersion: String,
    @SerialName("continue")
    val continueToken: String? = null,
    val remainingItemCount: Long? = null,
)