package org.builtonaws.kotlin.emf.logger

import io.github.oshai.kotlinlogging.KotlinLogging
import org.builtonaws.kotlin.emf.JavaMetricsLogger
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ThreadLocalMetricsContextProvider {
    companion object {
        internal val contexts = ConcurrentHashMap<String, JavaMetricsLogger>()
        internal val localContextKey = ThreadLocal<String>()
    }

    private val log = KotlinLogging.logger { }

    fun <T> runWithMetricsContext(runFunction: () -> T): () -> T =
        {
            val logger = JavaMetricsLogger()
            val result = runWithMetricsContext(logger, runFunction)
            logger.flush()
            result
        }

    internal fun <T> runWithMetricsContext(
        logger: JavaMetricsLogger,
        runFunction: () -> T,
    ): T {
        val previousKey = localContextKey.get()
        val key = UUID.randomUUID().toString()
        log.info { "Providing new metrics context key $key. Previous key: $previousKey." }
        contexts[key] = logger
        localContextKey.set(key)
        val result = runFunction()
        log.info { "Finished running function with metrics context key $key. Restoring previous key: $previousKey." }
        if (previousKey != null) {
            localContextKey.set(previousKey)
        } else {
            localContextKey.remove()
        }
        return result
    }
}
