package software.amazon.cloudwatchlogs.emf.model

/**
 * Side loads an equality function for the library's version of DimensionSet, for  use in testing.
 */
internal fun DimensionSet.overrideEquals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is DimensionSet) return false
    return dimensionRecords == other.dimensionRecords
}
