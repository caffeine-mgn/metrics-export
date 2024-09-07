package pw.binom

import kotlinx.coroutines.flow.Flow
import pw.binom.dto.PodDto

interface KubernetesClient {
    suspend fun getPods(nameSpace: String, chunkSize: Int = 100): Flow<PodDto>
}