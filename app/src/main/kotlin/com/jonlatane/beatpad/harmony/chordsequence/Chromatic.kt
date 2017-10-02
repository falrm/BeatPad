package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.Orbit
import com.jonlatane.beatpad.harmony.chord.Chord

object Chromatic : Orbit {
    override fun forward(c: Chord) = Chord(c.root + 1, c.extension)
    override fun back(c: Chord) = Chord(c.root - 1, c.extension)
}