package com.jonlatane.beatpad.model.harmony.chordsequence

import com.jonlatane.beatpad.model.harmony.Orbit
import com.jonlatane.beatpad.model.harmony.chord.Chord

object CircleOfFourths: Orbit {
    override fun forward(c: Chord): Chord {
        return Chord(c.root + 7, c.extension)
    }

    override fun back(c: Chord): Chord {
        return Chord(c.root - 7, c.extension)
    }
}