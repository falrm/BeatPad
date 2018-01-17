package com.jonlatane.beatpad.midi

import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import org.apache.commons.lang3.reflect.FieldUtils
import org.apache.commons.lang3.text.WordUtils
import org.billthefarmer.mididriver.GeneralMidiConstants
import java.util.*

object GM1Effects {
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

	fun randomInstrument(
		channel: Byte = 0,
		exceptions: Set<Byte> = emptySet()
	): MIDIInstrument {
		val options = ((0..127).map {it.toByte()} - exceptions)
		val instrument = options[Random().nextInt(options.size - 1)]
		return MIDIInstrument(channel, instrument)
	}
}