package com.jonlatane.beatpad.model.orbifold.orbit

import com.jonlatane.beatpad.model.orbifold.Orbit
import com.jonlatane.beatpad.model.chord.*

/**
 * Two Five One is simple: Every chord is major, minor or dominant.  Treat these as I, ii and V.
 */
object FunctionalTwoFiveOne : Orbit {
	override fun forward(c: Chord) = when {
		c.isMinor -> when {
			c.hasMajor7 -> c.changeRoot(M2).with(m3).replaceOrAdd(P5, d5) // imM7 -> iib5
			c.hasMinor2 -> c.changeRoot(-P5) // iii -> vi
			c.hasMinor6 -> c.changeRoot(-P5) // vi -> ii (irreversible)
			else -> c.changeRoot(-P5).with(M3, P5).remove(P4) // ii -> V
		}
		c.isDominant -> when {
			c.hasMinor2 -> c.changeRoot(-P5).withThird(MINOR) // V7b9 -> i
			else -> c.changeRoot(-P5).remove(P4).withThird(MAJOR) // V7 -> I
		}
		c.isSus -> when {
			else -> c
		}
		else -> when { // Assumed to be major at this
			c.hasAugmented4 -> c.changeRoot(M2) // VI -> V
			c.hasMajor3 -> c.changeRoot(M2).substituteIfPresent(M3, m3) // I -> ii
			else -> c.changeRoot(M2).with(m3) // I -> ii
		}
	}.autoP5

	override fun back(c: Chord) = when {
		c.isMinor -> when {
			c.hasMajor7 -> c.changeRoot(P5).with(P5).remove(P4)
				.substituteIfPresent(M2, m2) // imM7 -> V7b9
			c.hasMinor2 -> c.changeRoot(P5) // iii -> ?
			c.hasMinor6 -> c.changeRoot(P5) // vi -> iii
			else -> c.changeRoot(-M2) // ii -> I
				.with(M3).remove(P4).substituteIfPresent(m7, M7)
				.autoP5.autoM6
		}
		c.isDominant -> c.changeRoot(P5).remove(P4)
		c.isSus -> when {
			else -> c
		}
		else -> when { // Assumed to be major at this
			c.hasAugmented4 -> c.changeRoot(P5).with(P5) // VI -> V
			c.hasMajor3 -> when {

				else -> c.changeRoot(P5).remove(P4).replaceOrAdd(M7, m7) // I -> ii
			}
			else -> c.changeRoot(M2).with(m3) // I -> ii
		}
	}.autoP5
}