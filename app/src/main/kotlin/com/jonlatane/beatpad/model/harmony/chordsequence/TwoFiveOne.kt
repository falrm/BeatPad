package com.jonlatane.beatpad.model.harmony.chordsequence

import com.jonlatane.beatpad.model.harmony.Orbit
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.harmony.chord.Dom7
import com.jonlatane.beatpad.model.harmony.chord.Maj7
import com.jonlatane.beatpad.model.harmony.chord.min7

/**
 * Two Five One is simple: Every chord is major, minor or dominant.  Treat these as I, ii and V.
 */
object TwoFiveOne : Orbit {
    override fun forward(c: Chord) = when {
        c.isMinor -> Chord(c.root - 7, Dom7)
        c.isDominant -> Chord(c.root - 7, Maj7)
        else -> Chord(c.root + 2, min7)
    }

    override fun back(c: Chord) = when {
        c.isDominant -> Chord(c.root + 7, min7)
        c.isMinor -> Chord(c.root - 2, Maj7)
        else -> Chord(c.root + 7, Dom7)
    }
}