package com.jonlatane.beatpad.model.orbifold.orbit

import com.jonlatane.beatpad.model.orbifold.Orbit
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.model.chord.Maj7
import com.jonlatane.beatpad.model.chord.min7

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