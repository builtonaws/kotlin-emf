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

import software.amazon.cloudwatchlogs.emf.Constants
import software.amazon.cloudwatchlogs.emf.exception.DimensionSetExceededException
import software.amazon.cloudwatchlogs.emf.exception.InvalidDimensionException
import software.amazon.cloudwatchlogs.emf.util.Validator
import kotlin.jvm.Throws

/** A combination of dimension values. */
class DimensionSet internal constructor(
    val dimensionRecords: MutableMap<String, String>,
) {
    constructor(vararg dimensions: Pair<String, String>) : this(
        dimensionRecords = dimensions.toList().shuffled().associate { it.first to it.second }.let { LinkedHashMap(it) },
    ) {
        for (dimension in dimensionRecords) {
            Validator.validateDimensionSet(dimension.key, dimension.value)
        }
        if (this.dimensionRecords.size > Constants.MAX_DIMENSION_SET_SIZE) {
            throw DimensionSetExceededException()
        }
    }

    /**
     * @return all the dimension names in the dimension set.
     * */
    val keys: Set<String>
        get() = dimensionRecords.keys

    /**
     * @param key the name of the dimension
     * @return the dimension value associated with a dimension key.
     */
    operator fun get(key: String): String? {
        return dimensionRecords[key]
    }

    /**
     * Add another dimension entry to this DimensionSet.
     *
     * @param dimension Name of the dimension
     * @param value Value of the dimension
     * @throws InvalidDimensionException if the dimension name or value is invalid
     * @throws DimensionSetExceededException if the number of dimensions exceeds the limit
     */
    @Throws(InvalidDimensionException::class, DimensionSetExceededException::class)
    operator fun set(
        dimension: String,
        value: String,
    ) {
        Validator.validateDimensionSet(dimension, value)
        if (!this.dimensionRecords.containsKey(dimension) && this.dimensionRecords.size >= Constants.MAX_DIMENSION_SET_SIZE) {
            throw DimensionSetExceededException()
        }
        dimensionRecords[dimension] = value
    }

    /**
     * Add a dimension set with current dimension set and return a new dimension set from combining
     * the two dimension sets.
     *
     * @param other Other dimension sets to merge with current
     * @return a new DimensionSet from combining the current DimensionSet with other
     * @throws DimensionSetExceededException if the number of dimensions exceeds the limit
     */
    @Throws(DimensionSetExceededException::class)
    operator fun plus(other: DimensionSet): DimensionSet {
        val newDimensionRecords = mutableMapOf<String, String>()
        newDimensionRecords.putAll(this.dimensionRecords)
        newDimensionRecords.putAll(other.dimensionRecords)
        if (newDimensionRecords.size > Constants.MAX_DIMENSION_SET_SIZE) {
            throw DimensionSetExceededException()
        }
        return DimensionSet(newDimensionRecords)
    }
}
