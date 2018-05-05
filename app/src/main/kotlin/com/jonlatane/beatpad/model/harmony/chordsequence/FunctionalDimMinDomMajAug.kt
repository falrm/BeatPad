package com.jonlatane.beatpad.model.harmony.chordsequence

import com.jonlatane.beatpad.model.harmony.Orbit
import com.jonlatane.beatpad.model.harmony.chord.*

/**
 * Two Five One is simple: Every chord is major, minor or dominant.  Treat these as I, ii and V.
 */
object FunctionalDimMinDomMajAug : Orbit {
    override fun forward(c: Chord) = when {
        c.isDiminished -> c.replaceOrAdd(d5, P5)
        c.isMinor -> c.replaceOrAdd(m3, M3).replaceOrAdd(M7, m7)
        c.isDominant -> c.replaceOrAdd(m7, M7)
        else -> c.replaceOrAdd(P5, A5)
    }

    override fun back(c: Chord) = when {
        c.isAugmented -> c.replaceOrAdd(A5, P5)
        c.isMinor -> c.replaceOrAdd(P5, d5)
        c.isMajor && !c.isDominant -> c.replaceOrAdd(M7, m7)
        else -> c.replaceOrAdd(M3, m3)
    }
}