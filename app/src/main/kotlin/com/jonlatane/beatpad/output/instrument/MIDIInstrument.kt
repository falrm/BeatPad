package com.jonlatane.beatpad.output.instrument


import com.fasterxml.jackson.annotation.JsonIgnore
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.midi.AndroidMidi.CONTROL_CHANGE
import com.jonlatane.beatpad.midi.AndroidMidi.CONTROL_LSB
import com.jonlatane.beatpad.midi.AndroidMidi.CONTROL_MSB
import com.jonlatane.beatpad.midi.AndroidMidi.DEFAULT_VELOCITY
import com.jonlatane.beatpad.midi.AndroidMidi.NOTE_OFF
import com.jonlatane.beatpad.midi.AndroidMidi.NOTE_ON
import com.jonlatane.beatpad.midi.AndroidMidi.PROGRAM_CHANGE
import com.jonlatane.beatpad.midi.GM1Effects.MIDI_INSTRUMENT_NAMES
import com.jonlatane.beatpad.midi.GM2Effects
import com.jonlatane.beatpad.midi.MidiDevices
import com.jonlatane.beatpad.model.Instrument
import kotlin.experimental.or

class MIDIInstrument(
	var channel: Byte = 0,
	var instrument: Byte = 0
) : Instrument {
	@Transient private val tones = mutableListOf<Int>()
	@Transient private val byte2 = ByteArray(2)
	@Transient private val byte3 = ByteArray(3)
	override val type get() = "midi"

	object GM2Configuration {
		var msb: Byte? = null
		var lsb: Byte? = null
	}

	override fun play(tone: Int) {// Construct a note ON message for the middle C at maximum velocity on channel 1:
		play(tone, DEFAULT_VELOCITY)
	}

	override fun play(tone: Int, velocity: Int) {// Construct a note ON message for the middle C at maximum velocity on channel 1:
		selectInstrument(instrument)
		byte3[0] = NOTE_ON or channel  // STATUS byte: note On, 0x00 = channel 1
		byte3[1] = (tone + 60).toByte() // DATA byte: middle C = 60
		byte3[2] = velocity.toByte()  // DATA byte: maximum velocity = 127

		// Send the MIDI byte3 to the synthesizer.
		AndroidMidi.write(byte3)
		MidiDevices.send(byte3)
		tones.add(tone)
	}

	override fun stop() {
		for (tone in tones) {
			stop(tone)
		}
		tones.clear()
	}

	override fun stop(tone: Int) {
		// Construct a note OFF message for the middle C at minimum velocity on channel 1:
		byte3[0] = (NOTE_OFF or channel)  // STATUS byte: 0x80 = note Off, 0x00 = channel 1
		byte3[1] = (tone + 60).toByte()  // 0x3C = middle C
		byte3[2] = 0x00.toByte()  // 0x00 = the minimum velocity (0)

		// Send the MIDI byte3 to the synthesizer.
		AndroidMidi.write(byte3)
		MidiDevices.send(byte3)
	}

	override val instrumentName: String
		@JsonIgnore get() = if(GM2Configuration.msb != null) GM2Effects.all.find {
			it.patchNumber == instrument.toInt()
		}?.patchName ?: ""
		else MIDI_INSTRUMENT_NAMES[instrument.toInt()]

	private fun selectInstrument(instrument: Byte): MIDIInstrument {
		this.instrument = instrument
		// Write Bank MSB Control Change
		val msb = GM2Configuration.msb
		if (msb != null) {
			byte3[0] = (CONTROL_CHANGE or channel)
			byte3[1] = CONTROL_MSB
			byte3[2] = msb
			AndroidMidi.write(byte3)
			MidiDevices.send(byte3)
		}

		// Write Bank MSB Control Change
		val lsb = GM2Configuration.msb
		if (lsb != null) {
			byte3[0] = (CONTROL_CHANGE or channel)
			byte3[1] = CONTROL_LSB
			byte3[2] = lsb
			AndroidMidi.write(byte3)
			MidiDevices.send(byte3)
		}

		// Then write as Program Change
		byte2[0] = (PROGRAM_CHANGE or channel)  // STATUS byte: Change, 0x00 = channel 1
		byte2[1] = instrument
		AndroidMidi.write(byte2)
		MidiDevices.send(byte2)
		return this
	}
}
