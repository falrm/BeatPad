package com.jonlatane.beatpad.output.instrument


import com.fasterxml.jackson.annotation.JsonIgnore
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.midi.MidiConstants.CONTROL_CHANGE
import com.jonlatane.beatpad.midi.MidiConstants.CONTROL_LSB
import com.jonlatane.beatpad.midi.MidiConstants.CONTROL_MSB
import com.jonlatane.beatpad.midi.MidiConstants.CONTROL_VOLUME
import com.jonlatane.beatpad.midi.MidiConstants.DEFAULT_VELOCITY
import com.jonlatane.beatpad.midi.MidiConstants.NOTE_OFF
import com.jonlatane.beatpad.midi.MidiConstants.NOTE_ON
import com.jonlatane.beatpad.midi.MidiConstants.PROGRAM_CHANGE
import com.jonlatane.beatpad.midi.GM1Effects.MIDI_INSTRUMENT_NAMES
import com.jonlatane.beatpad.midi.GM2Effects
import com.jonlatane.beatpad.model.Instrument
import java.util.*
import kotlin.experimental.or

class MIDIInstrument constructor(
	@Transient var channel: Byte = 0,
	var instrument: Byte = 0,
	var drumTrack: Boolean = false
) : Instrument {
	override var volume: Float = 1f
		set(value) {
			field = when {
				value < 0f -> 0f
				value > 1f -> 1f
				else -> value
			}
		}
	override val type get() = "midi"
	@Transient private val tones: MutableList<Int> = Collections.synchronizedList(mutableListOf<Int>())
	@Transient private val byte2 = ByteArray(2)
	@Transient private val byte3 = ByteArray(3)

	object GM2Configuration {
		var msb: Byte? = null
		var lsb: Byte? = null
	}

	override fun play(tone: Int) {// Construct a note ON message for the middle C at maximum velocity on channel 1:
		play(tone, DEFAULT_VELOCITY)
	}

	override fun play(tone: Int, velocity: Int) {// Construct a note ON message for the middle C at maximum velocity on channel 1:
		//sendSelectInstrument(instrument)
		byte3[0] = NOTE_ON or channel  // STATUS byte: note On, 0x00 = channel 1
		byte3[1] = (tone + 60).toByte() // DATA byte: middle C = 60
		byte3[2] = velocity.toByte()  // DATA byte: maximum velocity = 127

		// Send the MIDI byte3 to the synthesizer.
		AndroidMidi.send(byte3)
		tones.add(tone)
	}

	override fun stop() {
		while(tones.isNotEmpty()) {
			val tone = tones.removeAt(0)
      doStop(tone)
		}
	}

  override fun stop(tone: Int) {
    doStop(tone)
    tones.remove(tone)
  }

  private fun doStop(tone: Int) {
		// Construct a note OFF message for the middle C at minimum velocity on channel 1:
		byte3[0] = (NOTE_OFF or channel)  // STATUS byte: 0x80 = note Off, 0x00 = channel 1
		byte3[1] = (tone + 60).toByte()  // 0x3C = middle C
		byte3[2] = 0x00.toByte()  // 0x00 = the minimum velocity (0)

		// Send the MIDI byte3 to the synthesizer.
		AndroidMidi.send(byte3)
	}

	override val instrumentName: String
		@JsonIgnore get() = if(GM2Configuration.msb != null) GM2Effects.all.find {
			it.patchNumber == instrument.toInt()
		}?.patchName ?: ""
		else if(drumTrack) "Drums" else MIDI_INSTRUMENT_NAMES[instrument.toInt()]

  fun sendSelectInstrument() = sendSelectInstrument(instrument)
	private fun sendSelectInstrument(instrument: Byte): MIDIInstrument {
		//if(!drumTrack) {
			// Write Bank MSB Control Change
			val msb = GM2Configuration.msb ?: if(drumTrack) 120.toByte() else null
			if (msb != null) {
				byte3[0] = (CONTROL_CHANGE or channel)
				byte3[1] = CONTROL_MSB
				byte3[2] = msb
				AndroidMidi.send(byte3)
			}

			// Write Bank MSB Control Change
			val lsb = GM2Configuration.lsb ?: if(drumTrack) 0.toByte() else null
			if (lsb != null) {
				byte3[0] = (CONTROL_CHANGE or channel)
				byte3[1] = CONTROL_LSB
				byte3[2] = lsb
				AndroidMidi.send(byte3)
			}

			// Then send as Program Change
			byte2[0] = (PROGRAM_CHANGE or channel)  // STATUS byte: Change, 0x00 = channel 1
			byte2[1] = if(drumTrack) 0 else instrument
			AndroidMidi.send(byte2)

			byte3[0] = (CONTROL_CHANGE or channel)
			byte3[1] = CONTROL_VOLUME
			byte3[2] = (volume * 127).toByte()
			AndroidMidi.send(byte3)
		//}
		return this
	}
}
