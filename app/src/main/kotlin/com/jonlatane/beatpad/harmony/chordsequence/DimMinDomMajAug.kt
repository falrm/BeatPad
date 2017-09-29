package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.Orbit
import com.jonlatane.beatpad.harmony.chord.Chord

object DimMinDomMajAug : Orbit {
    override fun forward(c: Chord) = when {
        c.isDiminished -> c.replaceOrAdd(6, 7)
        c.isMinor -> c.replaceOrAdd(3, 4).replaceOrAdd(11, 10)
        c.isDominant -> c.replaceOrAdd(10, 11)
        else -> c.replaceOrAdd(7, 8)
    }

    override fun back(c: Chord) = when {
        c.isAugmented -> c.replaceOrAdd(8, 7)
        c.isMinor -> c.replaceOrAdd(7, 6)
        c.isMajor && !c.isDominant -> c.replaceOrAdd(11, 10)
        else -> c.replaceOrAdd(4, 3)
    }
}