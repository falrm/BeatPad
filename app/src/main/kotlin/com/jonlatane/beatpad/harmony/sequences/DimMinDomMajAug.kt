package com.jonlatane.beatpad.harmony.sequences

import com.jonlatane.beatpad.harmony.ChordSequence
import com.jonlatane.beatpad.harmony.chord.Chord

object DimMinDomMajAug: ChordSequence {
    override fun forward(c: Chord): Chord {
        if (c.isDiminished) {
            return c.replaceOrAdd(6, 7)
        }
        if (c.isMinor) {
            return c.replaceOrAdd(3, 4).replaceOrAdd(11, 10)
        }
        if (c.isDominant) {
            return c.replaceOrAdd(10, 11)
        }
        return c.replaceOrAdd(7, 8)
    }

    override fun back(c: Chord): Chord {
        if (c.isAugmented) {
            return c.replaceOrAdd(8, 7)
        }
        if (c.isMinor) {
            return c.replaceOrAdd(7, 6)
        }
        if (c.isMajor && !c.isDominant) {
            return c.replaceOrAdd(11, 10)
        }
        return c.replaceOrAdd(4, 3)
    }
}