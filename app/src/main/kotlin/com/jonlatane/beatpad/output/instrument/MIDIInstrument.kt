package com.jonlatane.beatpad.output.instrument

import com.jonlatane.beatpad.midi.MidiDevices
import com.jonlatane.beatpad.model.Instrument
import org.apache.commons.lang3.reflect.FieldUtils
import org.apache.commons.lang3.text.WordUtils
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.billthefarmer.mididriver.MidiDriver
import java.util.*

import kotlin.experimental.or

class MIDIInstrument(
	var channel: Byte = 0,
	var instrument: Byte = 0
) : Instrument {
    @Transient private val tones = mutableListOf<Int>()
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
        selectInstrument(instrument)
        byte3[0] = NOTE_ON or channel  // STATUS byte: note On, 0x00 = channel 1
        byte3[1] = (tone + 60).toByte() // DATA byte: middle C = 60
        byte3[2] = velocity.toByte()  // DATA byte: maximum velocity = 127

        // Send the MIDI byte3 to the synthesizer.
        DRIVER.write(byte3)
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
        DRIVER.write(byte3)
        MidiDevices.send(byte3)
    }

    override val instrumentName: String
        get() = MIDI_INSTRUMENT_NAMES[instrument.toInt()]

    private fun selectInstrument(instrument: Byte): MIDIInstrument {
        this.instrument = instrument
        // Write Bank MSB Control Change
        val msb = GM2Configuration.msb
        if(msb != null) {
            byte3[0] = (CONTROL_CHANGE or channel)
            byte3[1] = CONTROL_MSB
            byte3[2] = msb
            DRIVER.write(byte3)
            MidiDevices.send(byte3)
        }

        // Write Bank MSB Control Change
        val lsb = GM2Configuration.msb
        if(lsb != null) {
            byte3[0] = (CONTROL_CHANGE or channel)
            byte3[1] = CONTROL_LSB
            byte3[2] = lsb
            DRIVER.write(byte3)
            MidiDevices.send(byte3)
        }

        // Then write as Program Change
        byte2[0] = (PROGRAM_CHANGE or channel)  // STATUS byte: Change, 0x00 = channel 1
        byte2[1] = instrument
        DRIVER.write(byte2)
        MidiDevices.send(byte2)
        return this
    }

    companion object {
        val DRIVER = MidiDriver()
        const val NOTE_ON = 0x90.toByte()
        const val NOTE_OFF: Byte = 0x80.toByte()
        const val PROGRAM_CHANGE = 0xC0.toByte()
        const val CONTROL_CHANGE = 0xB0.toByte()
        const val CONTROL_MSB = 0x00.toByte()
        const val CONTROL_LSB = 0x20.toByte()
        const val DEFAULT_VELOCITY = 64
	      fun randomInstrument(
		      channel: Byte = 0,
		      exceptions: Set<Byte> = emptySet()
	      ): MIDIInstrument {
		      val options = ((0..127).map {it.toByte()} - exceptions)
		      val instrument = options[Random().nextInt(options.size - 1)]
		      return MIDIInstrument(channel, instrument)
	      }
        val MIDI_INSTRUMENT_NAMES: List<String> by lazy {
            val instrumentNames = arrayOfNulls<String>(128)
            val declaredFields = GeneralMidiConstants::class.java.declaredFields
            for (field in declaredFields) {
                if (java.lang.reflect.Modifier.isStatic(field.modifiers)) {
                    val name = WordUtils.capitalizeFully(field.name.replace('_', ' '))
                    var index = -1
                    try {
                        index = (FieldUtils.readStaticField(field) as Byte).toInt()
                    } catch (e: IllegalAccessException) {
                    }

                    instrumentNames[index] = name
                }
            }
            listOf(*instrumentNames).map {it!!}
        }
    }
}
