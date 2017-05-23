package com.jonlatane.beatpad.view.topology

import com.jonlatane.beatpad.harmony.*

/**
 * Convenient usage modes for [TopologyView].
 * Created by jonlatane on 5/19/17.
 */

fun TopologyView.basicMode() {
    removeSequence(CHAINSMOKERS)
    removeSequence(AUG_DIM)
    removeSequence(CIRCLE_OF_FIFTHS)
    removeSequence(WHOLE_STEPS)
    removeSequence(REL_MINOR_MAJOR)
    addSequence(0, TWO_FIVE_ONE)
}

fun TopologyView.intermediateMode() {
    removeSequence(CHAINSMOKERS)
    removeSequence(CIRCLE_OF_FIFTHS)
    removeSequence(WHOLE_STEPS)
    addSequence(0, AUG_DIM)
    addSequence(1, TWO_FIVE_ONE)
    addSequence(2, REL_MINOR_MAJOR)
}

fun TopologyView.advancedMode() {
    removeSequence(CHAINSMOKERS)
    addSequence(0, AUG_DIM)
    addSequence(1, CIRCLE_OF_FIFTHS)
    addSequence(2, TWO_FIVE_ONE)
    addSequence(3, WHOLE_STEPS)
    addSequence(4, REL_MINOR_MAJOR)
}

fun TopologyView.chainsmokersMode() {
    removeSequence(AUG_DIM)
    removeSequence(WHOLE_STEPS)
    removeSequence(TWO_FIVE_ONE)
    addSequence(0, CIRCLE_OF_FIFTHS)
    addSequence(1, CHAINSMOKERS)
    addSequence(2, REL_MINOR_MAJOR)
}