package com.jonlatane.beatpad.model.orbifold.orbit

import com.jonlatane.beatpad.model.orbifold.Orbit
import com.jonlatane.beatpad.model.chord.AUGMENTED
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.model.chord.MAJOR
import com.jonlatane.beatpad.model.chord.NONEXISTENT

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

