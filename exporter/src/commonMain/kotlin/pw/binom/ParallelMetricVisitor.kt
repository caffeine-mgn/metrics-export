package pw.binom

import pw.binom.coroutines.SimpleAsyncLock
import pw.binom.io.AsyncWriter
import pw.binom.metric.prometheus.AsyncMetricVisitor
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

    override suspend fun help(text: String) {
        visitor.help(text)
    }

    override suspend fun start(name: String) {
        visitor.start(name)
    }

    override suspend fun type(text: String) {
        visitor.type(text)
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

    override suspend fun help(text: String) {
        writer.help(text)
    }

    override suspend fun start(name: String) {
        lock.lock()
        writer.start(name)
    }

    override suspend fun type(text: String) {
        writer.type(text)
    }

    override suspend fun value(value: String) {
        writer.value(value)
    }
}