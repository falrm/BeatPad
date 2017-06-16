package com.jonlatane.beatpad.harmony.sequences

import com.jonlatane.beatpad.harmony.ChordSequence
import com.jonlatane.beatpad.harmony.chord.Chord

object Chromatic : ChordSequence {
    override fun forward(c: Chord): Chord {
        return Chord(c.root + 1, c.extension)
    }

    override fun back(c: Chord): Chord {
        return Chord(c.root - 1, c.extension)
    }
}