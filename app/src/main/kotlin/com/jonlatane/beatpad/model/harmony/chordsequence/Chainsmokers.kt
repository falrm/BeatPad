package com.jonlatane.beatpad.model.harmony.chordsequence

import com.jonlatane.beatpad.model.harmony.Orbit
import com.jonlatane.beatpad.model.harmony.chord.*

// Based on "Something Just Like This"
object Chainsmokers: Orbit {
    override fun forward(c: Chord) = when {
        // D -> G(9)
        c.isMajor && c.heptatonics.second == NONEXISTENT
            -> Chord(c.root - 7, MajAdd9)
        // Bm -> D
        c.isMinor && !c.hasMinor7 -> Chord(c.root + 3, Maj)
        // Asus -> Bm
        c.isSus -> Chord(c.root + 2, min)
        // G(9) -> Asus
        else -> Chord(c.root + 2, sus)
    }

    override fun back(c: Chord) = when {
        // D -> Bm
        c.isMajor && c.heptatonics.second == NONEXISTENT
            -> Chord(c.root - 3, min)
        // Bm -> Asus
        c.isMinor && !c.hasMinor7 -> Chord(c.root - 2, sus)
        // Asus -> G(9)
        c.isSus -> Chord(c.root - 2, MajAdd9)
        // G(9) -> D
        else -> Chord(c.root + 7, Maj)
    }
}