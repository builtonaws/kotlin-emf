package org.builtonaws.kotlin.emf.logger

import org.builtonaws.kotlin.emf.JavaMetricsLogger
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
