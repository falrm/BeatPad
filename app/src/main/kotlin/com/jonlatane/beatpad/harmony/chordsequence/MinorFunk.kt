package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.ChordSequence
import com.jonlatane.beatpad.harmony.chord.*

/**
 * Based off of Stevie Wonder's "Superstition".
 *
 * Verse: Ebm7(add11) funk
 * Chorus: Bb7 (B7b5 - in alternate sequence) Bb7 A7(b5) Ab9 B7(#9)(#5)
 * return to Verse
 */
object MinorFunk : ChordSequence {
	override fun forward(c: Chord): Chord = when {
		c.isMinor -> Chord(c.root + 7, Dom9)
		c.isDominant -> Chord(c.root - 7, min11)
		else -> Chord(c.root, min11)
	}

	override fun back(c: Chord): Chord = forward(c)
}