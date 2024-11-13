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
package org.builtonaws.kotlin.emf.logger.org.builtonaws.kotlin.emf.logger

import org.builtonaws.kotlin.emf.JavaMetricsLogger
import org.builtonaws.kotlin.emf.MetricUnit
import org.builtonaws.kotlin.emf.logger.MetricsLogger
import org.builtonaws.kotlin.emf.logger.ThreadLocalMetricsContextProvider
import org.builtonaws.kotlin.emf.model.DimensionSet
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import software.amazon.cloudwatchlogs.emf.model.StorageResolution
import software.amazon.cloudwatchlogs.emf.model.overrideEquals
import java.time.Instant
import kotlin.test.assertEquals
import software.amazon.cloudwatchlogs.emf.model.DimensionSet as JavaDimensionSet

class MetricsTest {
    lateinit var javaMetricsLogger: JavaMetricsLogger
    lateinit var metricsLogger: MetricsLogger

    @BeforeEach
    fun setUp() {
        javaMetricsLogger = mock<JavaMetricsLogger>()
        metricsLogger = MetricsLogger(javaMetricsLogger)
    }

    @Test
    fun testContextKey() {
        val uuid = "bfbca18f-dfa4-4cdd-b50f-dc74a9b007d8"
        ThreadLocalMetricsContextProvider.localContextKey.set(uuid)
        assertEquals(uuid, metricsLogger.contextKey)
    }

    @Test
    fun testSetProperty() {
        val key = "key"
        val value = "value"
        metricsLogger.properties["key"] = value
        verify(javaMetricsLogger).putProperty(key, value)
    }

    @Test
    fun testPutDimensions() {
        val dimensionSet = DimensionSet("dimension1" to "value1", "dimension2" to "value2")
        val javaDimensionSet = JavaDimensionSet.of("dimension1", "value1", "dimension2", "value2")
        metricsLogger.dimensions += dimensionSet
        verify(javaMetricsLogger).putDimensions(argThat { overrideEquals(javaDimensionSet) })
    }

    @Test
    fun testSetDimensions() {
        val dimensionSet = DimensionSet("dimension1" to "value1", "dimension2" to "value2")
        val javaDimensionSet = JavaDimensionSet.of("dimension1", "value1", "dimension2", "value2")
        metricsLogger.dimensions.replace(dimensionSet)
        metricsLogger.dimensions.replace(dimensionSet, useDefault = true)
        metricsLogger.dimensions.replace(dimensionSet, useDefault = false)
        verify(javaMetricsLogger, times(2)).setDimensions(eq(false), argThat { overrideEquals(javaDimensionSet) })
        verify(javaMetricsLogger).setDimensions(eq(true), argThat { overrideEquals(javaDimensionSet) })
    }

    @Test
    fun testResetDimensions() {
        metricsLogger.dimensions.reset(true)
        metricsLogger.dimensions.reset(false)
        verify(javaMetricsLogger).resetDimensions(true)
        verify(javaMetricsLogger).resetDimensions(false)
    }

    @Test
    fun testPutMetricMinimal() {
        val name = "metric1"
        val metricValue = 1.0
        metricsLogger.metrics[name] = { value = metricValue }
        verify(javaMetricsLogger).putMetric(name, metricValue, MetricUnit.NONE, StorageResolution.STANDARD)
    }

    @Test
    fun testPutMetricAll() {
        val name = "metric1"
        val metricValue = 1.0
        metricsLogger.metrics[name] = {
            value = metricValue
            unit = MetricUnit.SECONDS
            storageResolution = StorageResolution.HIGH
        }
        verify(javaMetricsLogger).putMetric(name, metricValue, MetricUnit.SECONDS, StorageResolution.HIGH)
    }

    @Test
    fun testPutMetricFailsWithNoValue() {
        val name = "metric1"
        assertThrows(IllegalStateException::class.java) {
            metricsLogger.metrics[name] = {}
        }
        verify(javaMetricsLogger, never()).putMetric(eq(name), any(), any(), any())
    }

    @Test
    fun testPutMetadata() {
        val name = "metadata1"
        val value = "value"
        metricsLogger.metadata[name] = value
        verify(javaMetricsLogger).putMetadata(name, value)
    }

    @Test
    fun testSetNamespace() {
        val namespace = "namespace"
        metricsLogger.namespace(namespace)
        verify(javaMetricsLogger).setNamespace(namespace)
    }

    @Test
    fun testSetTimestamp() {
        val timestamp = Instant.now()
        metricsLogger.timestamp(timestamp)
        verify(javaMetricsLogger).setTimestamp(timestamp)
    }
}
