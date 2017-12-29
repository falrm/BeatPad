package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.Instrument
import com.jonlatane.beatpad.output.instrument.MIDIInstrument

/**
 * A [Palette] is simply a collection of unrelated [Chord]s
 * and [Part]s.
 */
class Palette {
    class Part(val instrument: Instrument = MIDIInstrument()) {
        val segments = mutableListOf<Pattern>()
    }
    val chords = mutableListOf<Chord>()
    val parts = mutableListOf<Part>()
}