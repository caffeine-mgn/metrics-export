package pw.binom

import pw.binom.coroutines.SimpleAsyncLock
import pw.binom.io.AsyncWriter
import pw.binom.metric.AsyncMetricVisitor
import pw.binom.metric.MetricType
import pw.binom.metric.prometheus.AsyncMetricWriter

class WithFieldAsyncMetricVisitor(
    val name: String,
    val value: String,
    val visitor: AsyncMetricVisitor,
) : AsyncMetricVisitor {
    override suspend fun end() {
        visitor.end()
    }

    override suspend fun field(name: String, value: String) {
        if (name == this.name) {
            return
        }
        visitor.field(name, value)
    }

    override suspend fun help(name: String, text: String) {
        visitor.help(name = name, text = text)
    }


    override suspend fun start(name: String) {
        visitor.start(name)
    }

    override suspend fun type(name: String, type: MetricType) {
        visitor.type(name = name, type = type)
    }

    override suspend fun value(value: String) {
        visitor.field(name = this.name, value = this.value)
        visitor.value(value)
    }
}

class ParallelMetricVisitor(writer: AsyncWriter) : AsyncMetricVisitor {
    private val writer = AsyncMetricWriter(writer)
    private val lock = SimpleAsyncLock()
    override suspend fun end() {
        writer.end()
        lock.unlock()
    }

    override suspend fun field(name: String, value: String) {
        writer.field(name, value)
    }

    override suspend fun help(name: String, text: String) {
        writer.help(metricName = name, text = text)
    }

    override suspend fun start(name: String) {
        lock.lock()
        writer.start(name)
    }

    override suspend fun type(name: String, type: MetricType) {
        writer.type(metricName = name, type = type)
    }

    override suspend fun value(value: String) {
        writer.value(value)
    }
}