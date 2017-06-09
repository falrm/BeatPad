package com.jonlatane.beatpad.harmony.sequences

import com.jonlatane.beatpad.harmony.ChordSequence
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.Maj7
import com.jonlatane.beatpad.harmony.chord.min7

object AlternatingMajorMinorSeconds: ChordSequence {
    override fun forward(c: Chord): Chord {
        if (c.isMinor) {
            return Chord(c.root + 1, Maj7)
        }
        return Chord(c.root + 2, min7)
    }

    override fun back(c: Chord): Chord {
        if (c.isMinor) {
            return Chord(c.root - 2, Maj7)
        }
        return Chord(c.root - 1, min7)
    }
}