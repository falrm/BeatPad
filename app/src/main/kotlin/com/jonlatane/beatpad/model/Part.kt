package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.output.instrument.MIDIInstrument

class Part(val instrument: Instrument = MIDIInstrument()) {
    val segments = mutableListOf<Pattern>()
}