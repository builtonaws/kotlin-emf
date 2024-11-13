/*
 *   Copyright João N. Matos. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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

private fun javaMetricsLogger(): JavaMetricsLogger? =
    ThreadLocalMetricsContextProvider
        .localContextKey.get()?.let {
            ThreadLocalMetricsContextProvider.contexts[it]
        }

val METRICS: MetricsLogger?
    get() =
        javaMetricsLogger()?.let {
            MetricsLogger(it)
        }

object Logging {
    private val logger: JavaMetricsLogger?
        get() = javaMetricsLogger()

    fun use(action: Logging.() -> Unit) {
        this.action()
    }

    /**
     * Set a property on the published metrics. This is stored in the emitted log data, and you are
     * not charged for this data by CloudWatch Metrics. These values can be values that are useful
     * for searching on, but have too high cardinality to emit as dimensions to CloudWatch Metrics.
     *
     * @param key Property name
     * @param value Property value
     * @return the current logger
     */
    fun putProperty(
        key: String,
        value: Any,
    ): Logging {
        logger?.putProperty(key, value)
        return this
    }

    /**
     * Adds a dimension. This is generally a low cardinality key-value pair that is part of the
     * metric identity. CloudWatch treats each unique combination of dimensions as a separate
     * metric, even if the metrics have the same metric name.
     *
     * @param dimensions the DimensionSet to add
     * @see <a
     *     href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/cloudwatch_concepts.html#Dimension">CloudWatch
     *     Dimensions</a>
     * @return the current logger
     */
    fun putDimensions(dimensions: DimensionSet): Logging {
        logger?.putDimensions(dimensions.toJava())
        return this
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
    fun setDimensions(
        vararg dimensionSets: DimensionSet,
        useDefault: Boolean = false,
    ): Logging {
        logger?.setDimensions(false, *dimensionSets.map { it.toJava() }.toTypedArray())
        return this
    }

    /**
     * Clear all custom dimensions on this MetricsLogger instance. Whether default dimensions should
     * be used can be configured by the input parameter.
     *
     * @param useDefault indicates whether default dimensions should be used
     * @return the current logger
     */
    fun resetDimensions(useDefault: Boolean): Logging {
        logger?.resetDimensions(useDefault)
        return this
    }

    /**
     * Put a metric value. This value will be emitted to CloudWatch Metrics asynchronously and does
     * not contribute to your account TPS limits. The value will also be available in your
     * CloudWatch Logs
     *
     * @param key is the name of the metric
     * @param value is the value of the metric
     * @param unit is the unit of the metric value
     * @param storageResolution is the resolution of the metric
     * @see <a
     *     href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/publishingMetrics.html#high-resolution-metrics">CloudWatch
     *     High Resolution Metrics</a>
     * @return the current logger
     * @throws InvalidMetricException if the metric is invalid
     */
    @Throws(InvalidMetricException::class)
    fun putMetric(
        key: String,
        value: Double,
        unit: MetricUnit = MetricUnit.NONE,
        storageResolution: StorageResolution = StorageResolution.STANDARD,
    ): Logging {
        logger?.putMetric(key, value, unit, storageResolution)
        return this
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
    fun putMetadata(
        key: String,
        value: Any,
    ): Logging {
        logger?.putMetadata(key, value)
        return this
    }

    /**
     * Set the CloudWatch namespace that metrics should be published to.
     *
     * @param namespace the namespace of the logs
     * @return the current logger
     * @throws InvalidNamespaceException if the namespace is invalid
     */
    @Throws(InvalidNamespaceException::class)
    fun setNamespace(namespace: String): Logging {
        logger?.setNamespace(namespace)
        return this
    }

    /**
     * Set the timestamp to be used for metrics.
     *
     * @param timestamp value of timestamp to be set
     * @return the current logger
     * @throws InvalidTimestampException if the timestamp is invalid
     */
    @Throws(InvalidTimestampException::class)
    fun setTimestamp(timestamp: Instant): Logging {
        logger?.setTimestamp(timestamp)
        return this
    }
}
