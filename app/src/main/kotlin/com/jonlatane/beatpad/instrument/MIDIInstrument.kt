package com.jonlatane.beatpad.instrument

import org.apache.commons.lang3.reflect.FieldUtils
import org.apache.commons.lang3.text.WordUtils
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.billthefarmer.mididriver.MidiDriver

import java.lang.reflect.Field
import java.util.LinkedList

import kotlin.experimental.or

/**
 * Created by jonlatane on 5/8/17.
 */
class MIDIInstrument : Instrument {

    private val tones = mutableListOf<Int>()
    private val byte2 = ByteArray(2)
    private val byte3 = ByteArray(3)
    var channel: Byte = 0
    var instrument: Byte = 0

    override fun play(tone: Int) {// Construct a note ON message for the middle C at maximum velocity on channel 1:
        play(tone, DEFAULT_VELOCITY)
    }

    fun play(tone: Int, velocity: Int) {// Construct a note ON message for the middle C at maximum velocity on channel 1:
        selectInstrument(instrument)
        byte3[0] = NOTE_ON or channel  // STATUS byte: note On, 0x00 = channel 1
        byte3[1] = (tone + 60).toByte() // DATA byte: middle C = 60
        byte3[2] = velocity.toByte()  // DATA byte: maximum velocity = 127

        // Send the MIDI byte3 to the synthesizer.
        DRIVER.write(byte3)
        tones.add(tone)
    }

    override fun stop() {
        for (tone in tones) {
            stop(tone.toInt())
        }
        tones.clear()
    }

    fun stop(tone: Int) {
        // Construct a note OFF message for the middle C at minimum velocity on channel 1:
        byte3[0] = (NOTE_OFF or channel).toByte()  // STATUS byte: 0x80 = note Off, 0x00 = channel 1
        byte3[1] = (tone + 60).toByte()  // 0x3C = middle C
        byte3[2] = 0x00.toByte()  // 0x00 = the minimum velocity (0)

        // Send the MIDI byte3 to the synthesizer.
        DRIVER.write(byte3)
    }

    val instrumentName: String
        get() = MIDI_INSTRUMENT_NAMES[instrument.toInt()]

    private fun selectInstrument(instrument: Byte): MIDIInstrument {
        this.instrument = instrument
        byte2[0] = (SELECT_INSTRUMENT or channel).toByte()  // STATUS byte: Change, 0x00 = channel 1
        byte2[1] = instrument
        DRIVER.write(byte2)
        return this
    }

    companion object {
        private val TAG = MIDIInstrument::class.simpleName
        val DRIVER = MidiDriver()
        val NOTE_ON = 0x90.toByte()
        val NOTE_OFF: Byte = 0x80.toByte()
        val SELECT_INSTRUMENT = 0xC0.toByte()
        val DEFAULT_VELOCITY = 64
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
