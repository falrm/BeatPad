package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.Orbit
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.Maj7
import com.jonlatane.beatpad.harmony.chord.min7

object AlternatingMajorMinorSeconds: Orbit {
    override fun forward(c: Chord) = when {
        c.isMinor -> Chord(c.root + 1, Maj7)
        else -> Chord(c.root + 2, min7)
    }

    override fun back(c: Chord) = when {
        c.isMinor -> Chord(c.root - 2, Maj7)
        else -> Chord(c.root - 1, min7)
    }
}