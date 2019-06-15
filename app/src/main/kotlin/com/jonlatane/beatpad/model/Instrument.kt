package com.jonlatane.beatpad.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.midi.GM1Effects
import com.jonlatane.beatpad.midi.GM2Effects
import com.jonlatane.beatpad.midi.MidiConstants
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import kotlin.experimental.or

/**
 * You should implement Instrument at the platform level,
 * including serialization/deserialization of your instrument.
 * MIDI Instruments are pretty well-understood and a standard interface
 * will probably be added soon.
 */
interface Instrument {
  fun play(tone: Int, velocity: Int)
  fun stop(tone: Int)
  fun play(tone: Int)
  fun stop()
  val type: String get() = "To be used during deserialization"
  val instrumentName get() = "Base Instrument"
  var volume: Float

  /**
   * So long as you can implement [send] on your platform, this implements the rest of
   * MIDI I/O in an [Instrument] in a way that should work with any synth.
   */
  interface Midi : Instrument {
    companion object {
      val byte2 = ByteArray(2)
      val byte3 = ByteArray(3)
    }

    var channel: Byte
    var instrument: Byte
    var drumTrack: Boolean
    val tones: MutableList<Int>
    val gm2Configuration: GM2Configuration

    fun send(data: ByteArray)

    class GM2Configuration(
      var msb: Byte? = null,
      var lsb: Byte? = null
    )

    override fun play(tone: Int) {// Construct a note ON message for the middle C at maximum velocity on channel 1:
      play(tone, MidiConstants.DEFAULT_VELOCITY)
    }

    override fun play(tone: Int, velocity: Int) {// Construct a note ON message for the middle C at maximum velocity on channel 1:
      //sendSelectInstrument(instrument)
      byte3[0] = MidiConstants.NOTE_ON or channel  // STATUS byte: note On, 0x00 = channel 1
      byte3[1] = (tone + 60).toByte() // DATA byte: middle C = 60
      byte3[2] = velocity.toByte()  // DATA byte: maximum velocity = 127

      // Send the MIDI byte3 to the synthesizer.
      send(byte3)
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
      byte3[0] = (MidiConstants.NOTE_OFF or channel)  // STATUS byte: 0x80 = note Off, 0x00 = channel 1
      byte3[1] = (tone + 60).toByte()  // 0x3C = middle C
      byte3[2] = 0x00.toByte()  // 0x00 = the minimum velocity (0)

      // Send the MIDI byte3 to the synthesizer.
      send(byte3)
    }

    fun sendSelectInstrument() {
      // Write Bank MSB Control Change
      val msb = gm2Configuration.msb ?: if (drumTrack) 120.toByte() else null
      if (msb != null) {
        byte3[0] = (MidiConstants.CONTROL_CHANGE or channel)
        byte3[1] = MidiConstants.CONTROL_MSB
        byte3[2] = msb
        send(byte3)
      }

      // Write Bank LSB Control Change
      val lsb = gm2Configuration.lsb ?: if (drumTrack) 0.toByte() else null
      if (lsb != null) {
        byte3[0] = (MidiConstants.CONTROL_CHANGE or channel)
        byte3[1] = MidiConstants.CONTROL_LSB
        byte3[2] = lsb
        send(byte3)
      }

      // Then send Program Change
      byte2[0] = (MidiConstants.PROGRAM_CHANGE or channel)  // STATUS byte: Change, 0x00 = channel 1
      byte2[1] = if (drumTrack) 0 else instrument
      send(byte2)

      byte3[0] = (MidiConstants.CONTROL_CHANGE or channel)
      byte3[1] = MidiConstants.CONTROL_VOLUME
      byte3[2] = (volume * 127).toByte()
      send(byte3)
    }
  }
}
