package com.jonlatane.beatpad.harmony

import com.jonlatane.beatpad.harmony.Topology.TopologyViewer
import com.jonlatane.beatpad.harmony.sequences.*

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

    interface TopologyViewer {
        fun addSequence(index: Int, sequence: ChordSequence)
        fun removeSequence(sequence: ChordSequence)
        fun useTopology(topology: Topology) {
            (allSequences - topology).forEach {
                removeSequence(it)
            }
            topology.indices.forEach {
                addSequence(it, topology[it])
            }
        }
    }

    companion object {
        private val allSequences: List<ChordSequence> = Topology.values().flatMap { it }
    }
}
