package pw.binom.services

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import pw.binom.KubernetesClient
import pw.binom.WithFieldAsyncMetricVisitor
import pw.binom.io.file.File
import pw.binom.io.file.readText
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.useAsync
import pw.binom.metric.prometheus.AsyncMetricVisitor
import pw.binom.metric.prometheus.PrometheusReader
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.inject
import pw.binom.url.toURL
import kotlin.coroutines.coroutineContext

class PodProviderService {
    companion object {
        const val ENABLE_ANNOTATION = "metric.binom.pw/enabled"
        const val URI_ANNOTATION = "metric.binom.pw/url"
        const val PROTO_ANNOTATION = "metric.binom.pw/proto"
        const val METHOD_ANNOTATION = "metric.binom.pw/method"
    }

    private val httpClient by inject<HttpClient>()


    private val kubernetesClient by inject<KubernetesClient>()

    private val nameSpace by BeanLifeCycle.afterInit {
        File("/var/run/secrets/kubernetes.io/serviceaccount/namespace").readText()
    }
    private val selfName by BeanLifeCycle.afterInit {
        File("/etc/hostname").readText().trim()
    }

    suspend fun getMetrics(visitor: AsyncMetricVisitor) {
        kubernetesClient.getPods(nameSpace = nameSpace)
            .filter { it.metadata.annotations[ENABLE_ANNOTATION] == "true" }
            .map { pod ->
                val proto = pod.metadata.annotations[PROTO_ANNOTATION] ?: "http"
                val path = pod.metadata.annotations[URI_ANNOTATION] ?: ":9090/metrics"
                val method = pod.metadata.annotations[METHOD_ANNOTATION] ?: "GET"
                GlobalScope.launch(coroutineContext) {
                    httpClient.connect(
                        method = method,
                        uri = "$proto://${pod.status.podIP}$path".toURL()
                    ).getResponse().useAsync { resp ->
                        resp.readText().useAsync { metricText ->
                            PrometheusReader.read(
                                reader = metricText,
                                visitor = WithFieldAsyncMetricVisitor(
                                    name = "pod",
                                    value = pod.metadata.name,
                                    visitor = visitor
                                )
                            )
                        }
                    }
                }
            }.toList().joinAll()
    }
}