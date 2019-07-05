package com.jonlatane.beatpad.model.orbifold.orbit

import com.jonlatane.beatpad.model.orbifold.Orbit
import com.jonlatane.beatpad.model.chord.*

/**
 * Two Five One is simple: Every chord is major, minor or dominant.  Treat these as I, ii and V.
 */
object FunctionalAlternatingMajorMinorThirds : Orbit {
    override fun forward(c: Chord) = when {
        c.isDominant -> c
        c.isMinor -> c.changeRoot(m3).substituteIfPresent(m3, M3)
        else -> c.changeRoot(M3).substituteIfPresent(M3, m3)
    }.autoP5

    override fun back(c: Chord) = when {
        c.isDominant -> c
        c.isMinor -> c.changeRoot(-M3).substituteIfPresent(m3, M3)
        else -> c.changeRoot(-m3).substituteIfPresent(M3, m3)
    }.autoP5
}