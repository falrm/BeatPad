package com.jonlatane.beatpad.output.instrument


import com.fasterxml.jackson.annotation.JsonIgnore
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.midi.GM1Effects.MIDI_INSTRUMENT_NAMES
import com.jonlatane.beatpad.midi.GM2Effects
import com.jonlatane.beatpad.model.Instrument
import com.jonlatane.beatpad.model.Instrument.Midi.GM2Configuration
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.*

class MIDIInstrument constructor(
	@Transient override var channel: Byte = 0,
	override var instrument: Byte = 0,
	override var drumTrack: Boolean = false,
	override val gm2Configuration: GM2Configuration = GM2Configuration()
) : Instrument.Midi, AnkoLogger {
	override var volume: Float = 1f
		set(value) {
			field = when {
				value < 0f -> 0f
				value > 1f -> 1f
				else -> value
			}
		}
	override val type get() = "midi"
	@Transient override val tones: MutableList<Int> = Collections.synchronizedList(mutableListOf<Int>())

	override fun send(data: ByteArray) {
		info("MIDI send: $data")
		AndroidMidi.sendStream.write(data)
	}

	override val instrumentName: String
		@JsonIgnore get() = if(gm2Configuration.msb != null) GM2Effects.all.find {
			it.patchNumber == instrument.toInt()
		}?.patchName ?: ""
		else if(drumTrack) "Drums" else MIDI_INSTRUMENT_NAMES[instrument.toInt()]
}
