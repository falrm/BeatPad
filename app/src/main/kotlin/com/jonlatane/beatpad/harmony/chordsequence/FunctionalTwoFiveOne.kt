package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.Orbit
import com.jonlatane.beatpad.harmony.chord.*

/**
 * Two Five One is simple: Every chord is major, minor or dominant.  Treat these as I, ii and V.
 */
object FunctionalTwoFiveOne : Orbit {
	override fun forward(c: Chord) = when {
		c.isMinor -> when {
			c.hasMajor7 -> c.changeRoot(M2).with(m3).replaceOrAdd(P5, d5) // imM7 -> iib5
			c.hasMinor2 -> c.changeRoot(-M2) // iii -> ii
			c.hasMinor6 -> c.changeRoot(-P5) // vi -> ii
			else -> c.changeRoot(-P5).with(M3) // ii -> V
		}
		c.isDominant -> c.changeRoot(-P5).remove(P4)
		c.isSus -> when {
			else -> c
		}
		else -> when { // Assumed to be major at this
			c.hasAugmented4 -> c.changeRoot(M2) // VI -> V
			c.hasMajor3 -> when {
				else -> c
			}
			else -> c.changeRoot(M2).with(m3) // I -> ii
		}
	}

	override fun back(c: Chord) = c
}