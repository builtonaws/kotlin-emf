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
import org.builtonaws.kotlin.emf.logger.METRICS
import org.builtonaws.kotlin.emf.logger.ThreadLocalMetricsContextProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID
import kotlin.test.assertEquals

class ThreadLocalMetricsContextProviderTest {
    @Test
    fun testHappyPath() {
        val originalUuid = "54d52a21-aa04-4862-9a5b-294a682a7862"
        val newUuidString = "12e0ea47-330e-40fd-9e4c-ad3126724585"
        val newUuid = UUID.fromString(newUuidString)
        val propertyName = "uuid"
        Mockito.mockStatic(UUID::class.java).use {
            ThreadLocalMetricsContextProvider.localContextKey.set(originalUuid)
            it.`when`<UUID> { UUID.randomUUID() }.thenReturn(newUuid)
            val logger = spy(JavaMetricsLogger())
            whenever(logger.putProperty(eq(propertyName), eq(newUuidString))).thenReturn(logger)
            val provider = ThreadLocalMetricsContextProvider()
            assertDoesNotThrow {
                provider.runWithMetricsContext(logger) {
                    logger.putProperty(propertyName, METRICS?.contextKey)
                }
            }
            it.verify { UUID.randomUUID() }
            verify(logger).putProperty(eq(propertyName), eq(newUuidString))
            assertEquals(originalUuid, ThreadLocalMetricsContextProvider.localContextKey.get())
        }
    }
}
