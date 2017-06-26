package com.jonlatane.beatpad.harmony

import com.jonlatane.beatpad.harmony.chordsequence.*

/**
 * A [Topology] is literally just a list of [ChordSequence]s. The [TopologyViewer] interface should
 * allow any given Android View to display a topology.
 *
 * Created by jonlatane on 5/26/17.
 */
enum class Topology(vararg sequences : ChordSequence) : List<ChordSequence> by listOf(*sequences) {
    basic(TwoFiveOne),
    intermediate(DimMinDomMajAug, TwoFiveOne, AlternatingMajorMinorThirds),
    advanced(DimMinDomMajAug, FourFiveOne, TwoFiveOne, AlternatingMajorMinorThirds, AlternatingMajorMinorSeconds),
    master(DimMinDomMajAug, SubdominantOptions, FourFiveOne, TwoFiveOne, TwoFiveOneMinor, AlternatingMajorMinorThirds, WholeSteps, AlternatingMajorMinorSeconds),
    chainsmokers(CircleOfFifths, Chainsmokers, AlternatingMajorMinorThirds),
    pop(SubdominantOptions, WholeSteps, AlternatingMajorMinorSeconds);

    companion object {
        internal val allSequences: List<ChordSequence> = Topology.values().flatMap { it }
    }
}
