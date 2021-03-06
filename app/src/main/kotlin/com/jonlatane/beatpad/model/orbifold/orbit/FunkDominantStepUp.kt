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
object FunkDominantStepUp : Orbit {
	override fun forward(c: Chord) = when {
		c.isDominant && !c.hasAugmented5 -> when {
			else -> Chord(c.root + 2, Dom7Sharp5 + A2)
		}
		else -> c
	}

	override fun back(c: Chord) = when {
		c.isDominant && c.hasAugmented5 -> Chord(c.root - 2, Dom9)
		else -> c
	}
}