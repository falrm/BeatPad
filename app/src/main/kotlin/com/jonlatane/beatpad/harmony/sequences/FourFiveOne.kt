package com.jonlatane.beatpad.harmony.sequences

import com.jonlatane.beatpad.harmony.ChordSequence
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.Dom7
import com.jonlatane.beatpad.harmony.chord.Maj6
import com.jonlatane.beatpad.harmony.chord.Maj7

/**
 * Like [TwoFiveOne] but less graceful. Assumes any M7 chord is a IV, any other non-dominant
 * major chord is a I and any minor chord is a iv.
 */
object FourFiveOne: ChordSequence {
    override fun forward(c: Chord): Chord {
        return when {
            c.isMinor -> Chord(c.root - 7, Dom7) // ii-V - is a discontinuity
            c.isDominant -> Chord(c.root - 7, Maj6) // V-I
            c.hasMajor7 -> Chord(c.root + 2, Dom7) // IV-V
            else -> Chord(c.root + 5, Maj7) // I-IV
        }
    }

    override fun back(c: Chord): Chord {
        return when {
            c.isMinor -> Chord(c.root - 7, Maj6) // ii-I
            c.isDominant -> Chord(c.root - 2, Maj7) // V-IV
            c.hasMajor7 -> Chord(c.root - 5, Maj6) // IV-I
            else -> Chord(c.root + 7, Dom7) // I-V
        }
    }
}