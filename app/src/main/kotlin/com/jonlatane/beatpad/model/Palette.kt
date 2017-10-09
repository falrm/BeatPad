package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.MIDIInstrument

/**
 * A [Palette] is simply a collection of unrelated [Chord]s
 * and [Part]s.
 */
class Palette {
    class Part {
        val instrument = MIDIInstrument()
        val segments = mutableListOf<ToneSequence>()
    }
    val chords = mutableListOf<Chord>()
    val parts = mutableListOf<Part>()
}