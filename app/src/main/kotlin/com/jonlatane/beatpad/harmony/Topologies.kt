package com.jonlatane.beatpad.harmony

/**
 * A [Topology] is literally just a list of [ChordSequence]s. The [TopologyViewer] interface should
 * allow any given Android View to display a topology.
 *
 * Created by jonlatane on 5/26/17.
 */
enum class Topology(vararg sequences : ChordSequence) : List<ChordSequence> by listOf(*sequences) {
    basic(TWO_FIVE_ONE),
    intermediate(AUG_DIM, TWO_FIVE_ONE, MAJOR_MINOR_THIRDS),
    advanced(AUG_DIM, CIRCLE_OF_FIFTHS, TWO_FIVE_ONE, WHOLE_STEPS, MAJOR_MINOR_THIRDS),
    chainsmokers(CIRCLE_OF_FIFTHS, CHAINSMOKERS, MAJOR_MINOR_THIRDS),
    pop(TWO_FIVE_ONE, CIRCLE_OF_FIFTHS, MAJOR_MINOR_THIRDS, WHOLE_STEPS, MAJOR_MINOR_SECONDS);

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
}

private val allSequences: List<ChordSequence> = Topology.values().flatMap { it }