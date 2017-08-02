package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.Orbit
import com.jonlatane.beatpad.harmony.chord.AUGMENTED
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.MAJOR
import com.jonlatane.beatpad.harmony.chord.NONEXISTENT

object FlatNaturalSharpNine: Orbit {
    override fun forward(c: Chord): Chord {
        when (c.heptatonics.second) {
            NONEXISTENT -> return c.plus(2)
            MAJOR -> return c.replaceOrAdd(2, 3)
            else -> return c.replaceOrAdd(1, 2)
        }
    }

    override fun back(c: Chord): Chord {
        when (c.heptatonics.second) {
            AUGMENTED -> return c.replaceOrAdd(3, 2)
            MAJOR -> return c.replaceOrAdd(2, 1)
            else -> return c.replaceOrAdd(2, 1)
        }
    }
}

