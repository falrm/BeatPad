package com.jonlatane.beatpad.harmony

/**
 * Created by jonlatane on 5/5/17.
 */
import com.jonlatane.beatpad.harmony.chord.*

interface ChordSequence {
    fun forward(c: Chord): Chord
    fun back(c: Chord): Chord
}