package com.jonlatane.beatpad.model.harmony.chordsequence

import com.jonlatane.beatpad.model.harmony.Orbit
import com.jonlatane.beatpad.model.harmony.chord.Chord

object Chromatic : Orbit {
    override fun forward(c: Chord) = Chord(c.root + 1, c.extension)
    override fun back(c: Chord) = Chord(c.root - 1, c.extension)
}