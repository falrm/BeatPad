package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.Instrument

class Palette {
    val chords = mutableListOf<Chord>()
    val instruments = mutableMapOf<Instrument, List<ToneSequence>>()
}