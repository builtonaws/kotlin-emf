package org.builtonaws.kotlin.emf.logger

import org.builtonaws.kotlin.emf.JavaMetricsLogger
import org.builtonaws.kotlin.emf.MetricUnit
import org.builtonaws.kotlin.emf.model.DimensionSet
import software.amazon.cloudwatchlogs.emf.exception.InvalidMetricException
import software.amazon.cloudwatchlogs.emf.exception.InvalidNamespaceException
import software.amazon.cloudwatchlogs.emf.exception.InvalidTimestampException
import software.amazon.cloudwatchlogs.emf.model.StorageResolution
import software.amazon.cloudwatchlogs.emf.model.toJava
import java.time.Instant
import kotlin.properties.Delegates

class MetricsLogger(internal val logger: JavaMetricsLogger) {
    /**
     * Returns the thread local key corresponding to this logger's context.
     * @throws NullPointerException if the context key is not set
     */
    val contextKey: String
        get() = ThreadLocalMetricsContextProvider.localContextKey.get()!!

    val properties = PropertiesLogger(logger)
    val dimensions = DimensionsLogger(logger)
    val metrics = MetricLogger(logger)
    val metadata = MetadataLogger(logger)

    /**
     * Set the CloudWatch namespace that metrics should be published to.
     *
     * @param namespace the namespace of the logs
     * @return the current logger
     * @throws InvalidNamespaceException if the namespace is invalid
     */
    @Throws(InvalidNamespaceException::class)
    fun namespace(namespace: String) {
        logger.setNamespace(namespace)
    }

    /**
     * Set the timestamp to be used for metrics.
     *
     * @param timestamp value of timestamp to be set
     * @return the current logger
     * @throws InvalidTimestampException if the timestamp is invalid
     */
    @Throws(InvalidTimestampException::class)
    fun timestamp(timestamp: Instant) {
        logger.setTimestamp(timestamp)
    }
}

class PropertiesLogger(internal val logger: JavaMetricsLogger) {
    /**
     * Set a property on the published metrics. This is stored in the emitted log data, and you are
     * not charged for this data by CloudWatch Metrics. These values can be values that are useful
     * for searching on, but have too high cardinality to emit as dimensions to CloudWatch Metrics.
     *
     * @param key Property name
     * @param value Property value
     * @return the current logger
     */
    operator fun set(
        key: String,
        value: Any,
    ) {
        logger.putProperty(key, value)
    }
}

class DimensionsLogger(internal val logger: JavaMetricsLogger) {
    /**
     * Adds a dimension. This is generally a low cardinality key-value pair that is part of the
     * metric identity. CloudWatch treats each unique combination of dimensions as a separate
     * metric, even if the metrics have the same metric name.
     *
     * @param dimensions the DimensionSet to add
     * @see <a
     *     href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/cloudwatch_concepts.html#Dimension">CloudWatch
     *     Dimensions</a>
     */
    operator fun plusAssign(dimensions: DimensionSet) {
        logger.putDimensions(dimensions.toJava())
    }

    /**
     * Overwrite all dimensions on this MetricsLogger instance.
     *
     * @param dimensionSets the dimensionSets to set
     * @param useDefault indicates whether default dimensions should be used
     * @see <a
     *     href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/cloudwatch_concepts.html#Dimension">CloudWatch
     *     Dimensions</a>
     * @return the current logger
     */
    fun replace(
        vararg dimensionSets: DimensionSet,
        useDefault: Boolean = false,
    ) {
        logger.setDimensions(useDefault, *dimensionSets.map { it.toJava() }.toTypedArray())
    }

    /**
     * Clear all custom dimensions on this MetricsLogger instance. Whether default dimensions should
     * be used can be configured by the input parameter.
     *
     * @param useDefault indicates whether default dimensions should be used
     * @return the current logger
     */
    fun reset(useDefault: Boolean) {
        logger.resetDimensions(useDefault)
    }
}

class MetricLogger(internal val logger: JavaMetricsLogger) {
    /**
     * Put a metric value. This value will be emitted to CloudWatch Metrics asynchronously and does
     * not contribute to your account TPS limits. The value will also be available in your
     * CloudWatch Logs
     *
     * @param key is the name of the metric
     * @param build closure to build the metric value
     * @see <a
     *     href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/publishingMetrics.html#high-resolution-metrics">CloudWatch
     *     High Resolution Metrics</a>
     * @return the current logger
     * @throws IllegalStateException if the metric value is not set within the build closure
     * @throws InvalidMetricException if the metric is invalid
     */
    @Throws(IllegalStateException::class, InvalidMetricException::class)
    operator fun set(
        key: String,
        build: MetricLogBuilder.() -> Unit,
    ) {
        val builder = MetricLogBuilder()
        builder.build()
        builder.accept(logger, key)
    }
}

/**
 * Add a custom key-value pair to the Metadata object.
 *
 * @see <a
 *     href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch_Embedded_Metric_Format_Specification.html#CloudWatch_Embedded_Metric_Format_Specification_structure_metadata">CloudWatch
 *     Metadata</a>
 * @param key the name of the key
 * @param value the value associated with the key
 * @return the current logger
 */
class MetadataLogger(internal val logger: JavaMetricsLogger) {
    operator fun set(
        key: String,
        value: Any,
    ) {
        logger.putMetadata(key, value)
    }
}

class MetricLogBuilder {
    var value by Delegates.notNull<Double>()
    var unit: MetricUnit = MetricUnit.NONE
    var storageResolution: StorageResolution = StorageResolution.STANDARD

    internal fun accept(
        logger: JavaMetricsLogger,
        key: String,
    ) {
        logger.putMetric(key, value, unit, storageResolution)
    }
}
