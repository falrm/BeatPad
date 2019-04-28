package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import java.util.*

class Part(val instrument: Instrument = MIDIInstrument()) {
	var id: UUID = UUID.randomUUID()
	val melodies = mutableListOf<Melody<*>>()
	var volume: Float
		get() = instrument.volume
		set(value) {
			instrument.volume = value
      (instrument as? MIDIInstrument)?.sendSelectInstrument()
		}
  val isDrumPart: Boolean get() = (instrument as? MIDIInstrument)?.drumTrack ?: false
}