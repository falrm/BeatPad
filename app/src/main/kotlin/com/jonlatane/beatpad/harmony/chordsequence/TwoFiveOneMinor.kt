package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.Orbit
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.Dom7
import com.jonlatane.beatpad.harmony.chord.min7
import com.jonlatane.beatpad.harmony.chord.minMaj7

/**
 * Two Five One is simple: Every chord is major, minor or dominant.  Treat these as I, ii and V.
 */
object TwoFiveOneMinor : Orbit {
	override fun forward(c: Chord) = when {
		c.isDiminished -> Chord(c.root - 7, Dom7).plus(1) // V7b9
		c.isDominant -> Chord(c.root - 7, minMaj7) // iM7
		else -> Chord(c.root + 2, min7).replaceOrAdd(7, 6) // ii7b5
	}

	override fun back(c: Chord) = when {
		c.isDiminished -> Chord(c.root - 2, minMaj7) // iM7
		c.isDominant -> Chord(c.root + 7, min7).replaceOrAdd(7, 6) // ii7b5
		else -> Chord(c.root + 7, Dom7).plus(1) // V7b9
	}
}