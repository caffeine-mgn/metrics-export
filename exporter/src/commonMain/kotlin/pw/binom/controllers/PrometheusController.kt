package pw.binom.controllers

import pw.binom.ParallelMetricVisitor
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.services.PodProviderService
import pw.binom.strong.inject
import pw.binom.url.toPath

class PrometheusController : HttpHandler {
    private val podProviderService by inject<PodProviderService>()
    override suspend fun handle(exchange: HttpServerExchange) {
        if (exchange.requestMethod != "GET") {
            return
        }
        if (exchange.requestURI.path != "/prometheus".toPath) {
            return
        }
        val resp = exchange.response()
        resp.status = 200
        resp.writeText { writer ->
            podProviderService.getMetrics(ParallelMetricVisitor(writer))
        }
    }

}