package com.jonlatane.beatpad.harmony.sequences

import com.jonlatane.beatpad.harmony.ChordSequence
import com.jonlatane.beatpad.harmony.chord.*

// Based on "Something Just Like This"
object Chainsmokers: ChordSequence {
    override fun forward(c: Chord): Chord {
        // D -> G(9)
        if (c.isMajor && c.heptatonics.second == NONEXISTENT) {
            return Chord(c.root - 7, MajAdd9)
        }
        // Bm -> D
        if (c.isMinor && !c.hasMinor7) {
            return Chord(c.root + 3, Maj)
        }
        // Asus -> Bm
        if (c.isSus) {
            return Chord(c.root + 2, min)
        }
        // G(9) -> Asus
        return Chord(c.root + 2, sus)
    }

    override fun back(c: Chord): Chord {
        // D -> Bm
        if (c.isMajor && c.heptatonics.second == NONEXISTENT) {
            return Chord(c.root - 3, min)
        }
        // Bm -> Asus
        if (c.isMinor && !c.hasMinor7) {
            return Chord(c.root - 2, sus)
        }
        // Asus -> G(9)
        if (c.isSus) {
            return Chord(c.root - 2, MajAdd9)
        }
        // G(9) -> D
        return Chord(c.root + 7, Maj)
    }
}