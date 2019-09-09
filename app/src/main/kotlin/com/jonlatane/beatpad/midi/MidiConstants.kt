package com.jonlatane.beatpad.midi

import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import org.apache.commons.lang3.reflect.FieldUtils
import org.apache.commons.lang3.text.WordUtils
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.billthefarmer.mididriver.MidiDriver
import java.util.*
import kotlin.experimental.and

object MidiConstants {
	const val NOTE_ON = 0x90.toByte()
	const val NOTE_OFF: Byte = 0x80.toByte()
	const val ALL_CONTROLLERS_OFF: Byte = 121.toByte()
	const val ALL_NOTES_OFF: Byte = 123.toByte()
	const val PROGRAM_CHANGE = 0xC0.toByte()
	const val CONTROL_CHANGE = 0xB0.toByte()
	const val CONTROL_MSB = 0x00.toByte()
	const val CONTROL_LSB = 0x20.toByte()
	const val CONTROL_VOLUME = 0x07.toByte()
	const val DEFAULT_VELOCITY = 64
	const val TICK = 0xF8.toByte()
	const val PLAY = 0xFA.toByte()
	const val STOP = 0xFC.toByte()
	const val SYNC = 0xFE.toByte()
	const val LEFT_MASK = 0xF0.toByte()
	const val RIGHT_MASK = 0x0F.toByte()
	inline val Byte.leftHalf: Byte get() = (this and LEFT_MASK)
	inline val Byte.rightHalf: Byte get() = (this and RIGHT_MASK)
	fun Byte.leftHalfMatches(value: Byte): Boolean = this.leftHalf == value.leftHalf
	fun Byte.leftHalfMatchesAny(vararg values: Byte): Boolean = values.any { leftHalfMatches(it) }
}