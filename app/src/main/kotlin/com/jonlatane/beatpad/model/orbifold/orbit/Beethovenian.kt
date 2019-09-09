package com.jonlatane.beatpad.model.orbifold.orbit

import com.jonlatane.beatpad.model.orbifold.Orbit
import com.jonlatane.beatpad.model.chord.*

/**
 * Based off of the first theme of the sonata (3rd) movement of
 * Beethoven's Moonlight sonata (Op. 27, No. 2).
 *
 * Chord progression:
 *  C#m G# C#7 F#m6(*) Adim7 G#13add11(**) A13
 *
 *  (*) This is to differentiate from C#m
 *  (**) From here, you can use
 */
object Beethovenian : Orbit {
	val progression = listOf(
		Chord(1, min)
	)
	override fun forward(c: Chord): Chord = c/*when {
		c.isMinor -> Chord(c.root + 7, Maj)
		c.isDominant -> Chord(c.root - P5, min)
		c.isMajor -> when {
			c.heptatonics.seventh == NONEXISTENT -> Chord(c.root - 7, Dom7)
			else -> TODO()
		}
	}*/

	override fun back(c: Chord): Chord = forward(c)
}