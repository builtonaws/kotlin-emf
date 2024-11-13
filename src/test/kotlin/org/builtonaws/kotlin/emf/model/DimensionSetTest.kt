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
package org.builtonaws.kotlin.emf.model

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import software.amazon.cloudwatchlogs.emf.exception.DimensionSetExceededException
import software.amazon.cloudwatchlogs.emf.model.toJava
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DimensionSetTest {
    @Test
    fun testAddDimension() {
        val dimensionsToBeAdded = 30
        val dimensionSet = generateDimensionSet(1, dimensionsToBeAdded)
        assertEquals(dimensionsToBeAdded, dimensionSet.keys.size)
    }

    @Test
    fun testAddDimensionLimitExceeded() {
        val exception =
            assertThrows(DimensionSetExceededException::class.java) {
                val dimensionSetSize = 33
                generateDimensionSet(1, dimensionSetSize)
            }
        val expectedMessage = "Maximum number of dimensions"
        val actualMessage = exception.message
        assertTrue { actualMessage?.contains(expectedMessage) ?: false }
    }

    @Test
    fun testMergeDimensionSets() {
        val exception =
            assertThrows(DimensionSetExceededException::class.java) {
                val dimensionSetSize = 28
                val otherDimensionSetSize = 5
                val dimensionSet = generateDimensionSet(1, dimensionSetSize)
                val otherDimensionSet = generateDimensionSet(2, otherDimensionSetSize)
                dimensionSet + otherDimensionSet
            }
        val expectedMessage = "Maximum number of dimensions"
        val actualMessage = exception.message
        assertTrue { actualMessage?.contains(expectedMessage) ?: false }
    }

    @Test
    fun toJavaTest() {
        val dimensionSet =
            DimensionSet(
                "Dimension1" to "value1",
                "Dimension2" to "value2",
            )
        val javaDimensionSet = dimensionSet.toJava()
        assertEquals(dimensionSet.keys, javaDimensionSet.dimensionKeys)
        assertEquals(dimensionSet["Dimension1"], javaDimensionSet.getDimensionValue("Dimension1"))
        assertEquals(dimensionSet["Dimension2"], javaDimensionSet.getDimensionValue("Dimension2"))
    }

    private fun generateDimensionSet(
        id: Int,
        numOfDimensions: Int,
    ): DimensionSet {
        val dimensionSet = DimensionSet()
        for (i in 0 until numOfDimensions) {
            dimensionSet["Dimension$id.$i"] = "value$i"
        }
        return dimensionSet
    }
}
