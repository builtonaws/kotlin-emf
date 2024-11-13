package software.amazon.cloudwatchlogs.emf.model

import org.builtonaws.kotlin.emf.model.DimensionSet
import software.amazon.cloudwatchlogs.emf.model.DimensionSet as JavaDimensionSet

/**
 * Maps the wrapper's version of DimensionSet to the library's version.
 */
internal fun DimensionSet.toJava(): JavaDimensionSet =
    JavaDimensionSet().also {
        it.dimensionRecords.putAll(this.dimensionRecords)
    }
