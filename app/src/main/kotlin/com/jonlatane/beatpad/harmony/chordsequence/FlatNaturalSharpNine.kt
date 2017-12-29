package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.Orbit
import com.jonlatane.beatpad.harmony.chord.AUGMENTED
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.MAJOR
import com.jonlatane.beatpad.harmony.chord.NONEXISTENT

object FlatNaturalSharpNine: Orbit {
    override fun forward(c: Chord) = when (c.heptatonics.second) {
        NONEXISTENT -> c.plus(2)
        MAJOR -> c.substituteIfPresent(2, 3)
        else -> c.substituteIfPresent(1, 2)
    }

    override fun back(c: Chord) = when (c.heptatonics.second) {
        AUGMENTED -> c.substituteIfPresent(3, 2)
        MAJOR -> c.substituteIfPresent(2, 1)
        else -> c.substituteIfPresent(2, 1)
    }
}

