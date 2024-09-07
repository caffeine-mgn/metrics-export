package pw.binom

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pw.binom.dto.PodDto
import pw.binom.dto.PodListDto
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.addHeader
import pw.binom.io.useAsync
import pw.binom.url.URL

class KubernetesClientImpl(
    httpClient: Lazy<HttpClient>,
    val baseUrl: URL,
    val token: String,
) : KubernetesClient {
    private val httpClient by httpClient

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private suspend fun <T : Any> get(
        url: String,
        k: KSerializer<T>,
        limit: Int = -1,
        continueToken: String? = null,
        resourceVersion: String? = null,
    ): T {
        var urlResult = baseUrl.appendPath(url)
        if (limit > 0) {
            urlResult = urlResult.appendQuery("limit", limit)
        }
        if (continueToken != null) {
            urlResult = urlResult.appendQuery("continue", continueToken)
        }
        if (resourceVersion != null) {
            urlResult = urlResult.appendQuery("resourceVersion", resourceVersion)
        }
        val req = httpClient.connect(
            method = "GET",
            uri = urlResult
        )
        req.addHeader("Authorization", "Bearer $token")
        val text = req.getResponse().useAsync {
            if (it.responseCode != 200) {
                TODO("responseCode=${it.responseCode}")
            }
            val l = it.readAllText()
            println(l)
            l
        }

        val jsonElement = json.parseToJsonElement(text)
        val kind = jsonElement.jsonObject["kind"]?.jsonPrimitive?.contentOrNull
        if (kind != k.descriptor.serialName) {
            TODO("Invalid kind $kind")
        }
        val podList = json.decodeFromJsonElement(k, jsonElement)
        return podList
    }

    override suspend fun getPods(nameSpace: String, chunkSize: Int): Flow<PodDto> {
        var continueToken: String? = null
        return flow {
            while (true) {
                val result = get(
                    url = "/api/v1/namespaces/$nameSpace/pods",
                    k = PodListDto.serializer(),
                    limit = chunkSize,
                    continueToken = continueToken
                )

                if (result.items.isEmpty()) {
                    break
                }
                result.items.forEach {
                    emit(it)
                }
                continueToken = result.metadata.continueToken
                if (continueToken == null) {
                    break
                }
            }
        }
    }
}