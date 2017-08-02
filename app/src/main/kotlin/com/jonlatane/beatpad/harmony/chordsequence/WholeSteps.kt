package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.Orbit
import com.jonlatane.beatpad.harmony.chord.Chord

object WholeSteps: Orbit {
    override fun forward(c: Chord): Chord {
        return Chord(c.root + 2, c.extension)
    }

    override fun back(c: Chord): Chord {
        return Chord(c.root - 2, c.extension)
    }
}