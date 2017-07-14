package com.jonlatane.beatpad.harmony

import com.jonlatane.beatpad.harmony.chordsequence.*

/**
 * A [Topology] is literally just a list of [ChordSequence]s. The [TopologyViewer] interface should
 * allow any given Android View to display a topology.
 *
 * Created by jonlatane on 5/26/17.
 */
enum class Topology(val title: String, vararg sequences : ChordSequence) : List<ChordSequence> by listOf(*sequences) {
    basic("Basic", TwoFiveOne),
    intermediate("Intermediate", DimMinDomMajAug, TwoFiveOne, AlternatingMajorMinorThirds),
    advanced("Advanced", DimMinDomMajAug, FourFiveOne, TwoFiveOne, AlternatingMajorMinorThirds, AlternatingMajorMinorSeconds),
    master("Master", DimMinDomMajAug, SubdominantOptions, FourFiveOne, TwoFiveOne, TwoFiveOneMinor, AlternatingMajorMinorThirds, WholeSteps, AlternatingMajorMinorSeconds),
    chainsmokers("Chainsmokers", CircleOfFifths, Chainsmokers, AlternatingMajorMinorThirds),
    pop("Pop", SubdominantOptions, WholeSteps, AlternatingMajorMinorSeconds);

    companion object {
        internal val allSequences: List<ChordSequence> = Topology.values().flatMap { it }
    }
}
