package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.harmony.chord.Chord

/**
 * A [Palette] is simply a collection of unrelated [Chord]s
 * and [Part]s.
 */
class Palette {
    val chords = mutableListOf<Chord>()
    val parts = mutableListOf<Part>()
}