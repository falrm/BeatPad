package com.jonlatane.beatpad.model.harmony

/**
 * Created by jonlatane on 5/5/17.
 */
import com.jonlatane.beatpad.model.harmony.chord.*

interface Orbit {
    fun forward(c: Chord): Chord
    fun back(c: Chord): Chord
}