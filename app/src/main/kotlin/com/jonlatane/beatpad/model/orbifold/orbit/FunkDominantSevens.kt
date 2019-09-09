package com.jonlatane.beatpad.model.orbifold.orbit

import com.jonlatane.beatpad.model.orbifold.Orbit
import com.jonlatane.beatpad.model.chord.*

/**
 * Based off of Stevie Wonder's "Superstition".
 *
 * Verse: Ebm7(add11) funk
 * Chorus: Bb7 (B7b5 - in alternate sequence) Bb7 A7(b5) Ab9 B7(#9)(#5)
 * return to Verse
 */
object FunkDominantSevens : Orbit {
	override fun forward(c: Chord) = when {
		c.isDominant -> when {
			c.hasDiminished5 -> Chord(c.root + 1, Dom9)
			else -> Chord(c.root + 1, Dom9Flat5)
		}
		else -> c
	}

	override fun back(c: Chord) = when {
		c.isDominant -> when {
			c.hasDiminished5 -> Chord(c.root - 1, Dom9)
			else -> Chord(c.root - 1, Dom9Flat5)
		}
		else -> c
	}
}