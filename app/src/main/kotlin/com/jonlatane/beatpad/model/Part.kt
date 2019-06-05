package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import java.util.*

class Part(val instrument: Instrument = MIDIInstrument()) {
	var id: UUID = UUID.randomUUID()
	val melodies: MutableList<Melody<*>> = mutableListOf()
	var volume: Float
		get() = instrument.volume
		set(value) {
			instrument.volume = value
      (instrument as? MIDIInstrument)?.sendSelectInstrument()
		}
	val drumTrack: Boolean get() = (instrument as? MIDIInstrument)?.drumTrack == true
}