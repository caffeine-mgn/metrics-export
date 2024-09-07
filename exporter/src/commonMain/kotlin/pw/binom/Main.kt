package pw.binom

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pw.binom.controllers.PrometheusController
import pw.binom.io.file.File
import pw.binom.io.file.readText
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.create
import pw.binom.network.MultiFixedSizeThreadNetworkDispatcher
import pw.binom.services.PodProviderService
import pw.binom.signal.Signal
import pw.binom.strong.Strong
import pw.binom.strong.bean
import pw.binom.strong.inject
import pw.binom.strong.plus
import pw.binom.strong.properties.StrongProperties
import pw.binom.strong.web.server.WebConfig
import pw.binom.url.toURL

val kubernetesClientConfig = Strong.config {
    it.bean {
        val host = Environment.getEnv("KUBERNETES_SERVICE_HOST")!!
        val port = Environment.getEnv("KUBERNETES_SERVICE_PORT_HTTPS")!!.toInt()
        KubernetesClientImpl(
            httpClient = it.inject(),
            baseUrl = "https://$host:$port/".toURL(),
            token = File("/var/run/secrets/kubernetes.io/serviceaccount/token").readText()
        )
    }
}

fun config(strongProperties: StrongProperties) = Strong.config {
    it.bean { strongProperties }
    it.bean { PrometheusController() }
    it.bean { PodProviderService() }
} + WebConfig.apply(strongProperties) + kubernetesClientConfig

fun main(args: Array<String>) {
    val networkManager = MultiFixedSizeThreadNetworkDispatcher(4)
    val httpClient = HttpClient.create(networkDispatcher = networkManager)
    runBlocking {
        val strong = Strong.create(
            config(
                StrongProperties()
                    .addArgs(args)
                    .addEnvironment()
            ) + Strong.config {
                it.bean { networkManager }
                it.bean { httpClient }
            })
        Signal.handler {
            if (it.isInterrupted) {
                GlobalScope.launch {
                    strong.destroy()
                }
            }
        }
        strong.awaitDestroy()
    }
}
