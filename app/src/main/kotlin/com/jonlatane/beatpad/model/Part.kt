package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.output.instrument.MIDIInstrument

class Part(val instrument: Instrument = MIDIInstrument()) {
	val melodies = mutableListOf<Melody>()
	var volume: Float
		get() = instrument.volume
		set(value) { instrument.volume = value }
}