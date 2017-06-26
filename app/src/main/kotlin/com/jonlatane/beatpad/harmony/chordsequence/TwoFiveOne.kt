package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.ChordSequence
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.Dom7
import com.jonlatane.beatpad.harmony.chord.Maj7
import com.jonlatane.beatpad.harmony.chord.min7

/**
 * Two Five One is simple: Every chord is major, minor or dominant.  Treat these as I, ii and V.
 */
object TwoFiveOne : ChordSequence {
	override fun forward(c: Chord): Chord {
		if (c.isMinor) {
			return Chord(c.root - 7, Dom7)
		}
		if (c.isDominant) {
			return Chord(c.root - 7, Maj7)
		}
		return Chord(c.root + 2, min7)
	}

	override fun back(c: Chord): Chord {
		if (c.isDominant) {
			return Chord(c.root + 7, min7)
		}
		if (c.isMinor) {
			return Chord(c.root - 2, Maj7)
		}
		return Chord(c.root + 7, Dom7)
	}
}