package com.jonlatane.beatpad.model.harmony.chordsequence

import com.jonlatane.beatpad.model.harmony.Orbit
import com.jonlatane.beatpad.model.harmony.chord.*

/**
 * Based off of Stevie Wonder's "Superstition".
 *
 * Verse: Ebm7(add11) funk
 * Chorus: Bb7 (B7b5 - in alternate sequence) Bb7 A7(b5) Ab9 B7(#9)(#5)
 * return to Verse
 */
object MinorFunk : Orbit {
	override fun forward(c: Chord) = when {
		c.isMinor -> Chord(c.root + 7, Dom9)
		c.isDominant -> Chord(c.root - 7, min11)
		else -> Chord(c.root, min11)
	}

	override fun back(c: Chord): Chord = forward(c)
}