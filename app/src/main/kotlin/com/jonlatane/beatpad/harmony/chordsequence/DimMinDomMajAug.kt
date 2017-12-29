package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.Orbit
import com.jonlatane.beatpad.harmony.chord.Chord

object DimMinDomMajAug : Orbit {
    override fun forward(c: Chord) = when {
        c.isDiminished -> c.substituteIfPresent(6, 7)
        c.isMinor -> c.substituteIfPresent(3, 4).substituteIfPresent(11, 10)
        c.isDominant -> c.substituteIfPresent(10, 11)
        else -> c.substituteIfPresent(7, 8)
    }

    override fun back(c: Chord) = when {
        c.isAugmented -> c.substituteIfPresent(8, 7)
        c.isMinor -> c.substituteIfPresent(7, 6)
        c.isMajor && !c.isDominant -> c.substituteIfPresent(11, 10)
        else -> c.substituteIfPresent(4, 3)
    }
}