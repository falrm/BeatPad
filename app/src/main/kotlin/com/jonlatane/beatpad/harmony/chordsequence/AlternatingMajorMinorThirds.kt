package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.Orbit
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.Maj7
import com.jonlatane.beatpad.harmony.chord.min7

object AlternatingMajorMinorThirds: Orbit {
    override fun forward(c: Chord): Chord {
        if (c.isMinor) {
            return Chord(c.root + 3, Maj7)
        }
        return Chord(c.root + 4, min7)
    }

    override fun back(c: Chord): Chord {
        if (c.isMinor) {
            return Chord(c.root - 4, Maj7)
        }
        return Chord(c.root - 3, min7)
    }
}