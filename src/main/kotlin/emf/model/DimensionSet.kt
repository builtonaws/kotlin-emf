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
