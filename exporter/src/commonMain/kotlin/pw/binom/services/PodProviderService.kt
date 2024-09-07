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
    private val httpClient by inject<HttpClient>()

    //    private val url = run {
//        val host = Environment.getEnv("KUBERNETES_SERVICE_HOST")!!
//        val port = Environment.getEnv("KUBERNETES_SERVICE_PORT_HTTPS")!!.toInt()
//        "https://$host:$port/".toURL()
//    }
    private val kubernetesClient by inject<KubernetesClient>()

    //    private val token by BeanLifeCycle.afterInit {
//        File("/var/run/secrets/kubernetes.io/serviceaccount/token").readText()
//    }
    private val nameSpace by BeanLifeCycle.afterInit {
        File("/var/run/secrets/kubernetes.io/serviceaccount/namespace").readText()
    }
    private val selfName by BeanLifeCycle.afterInit {
        File("/etc/hostname").readText().trim()
    }

    suspend fun getMetrics(a: AsyncMetricVisitor) {
        kubernetesClient.getPods(nameSpace = nameSpace)
            .filter { it.metadata.annotations["metric.binom.pw/enabled"]=="true" }
            .map { pod ->
                val path = pod.metadata.annotations["metric.binom.pw/url"]?:":9090/metrics"
            GlobalScope.launch(coroutineContext) {
                httpClient.connect(
                    method = "GET",
                    uri = "http://${pod.status.podIP}$path".toURL()
                ).getResponse().useAsync {
                    it.readText().useAsync {
                        PrometheusReader.read(
                            reader = it,
                            visitor = WithFieldAsyncMetricVisitor(
                                name = "pod",
                                value = pod.metadata.name,
                                visitor = a
                            )
                        )
                    }
                }
            }
        }.toList().joinAll()
    }

//    init {
//        BeanLifeCycle.postConstruct {
//        }
//    }
}