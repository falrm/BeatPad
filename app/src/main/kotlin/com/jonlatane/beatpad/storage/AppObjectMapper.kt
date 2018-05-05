package com.jonlatane.beatpad.storage

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.jonlatane.beatpad.model.Instrument
import com.jonlatane.beatpad.model.Melement
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.harmony.chord.Chord

object AppObjectMapper : ObjectMapper() {
	init {
		val module = SimpleModule().apply {
			addSerializer(Melody::class.java, MelodyStorage.Serializer)
			addDeserializer(Melement::class.java, MelodyStorage.ElementSerializer)
			addDeserializer(Melody::class.java, MelodyStorage.Deserializer)
			addDeserializer(Instrument::class.java, InstrumentStorage.Deserializer)
			addSerializer(Chord::class.java, ChordStorage.Serializer)
			addDeserializer(Chord::class.java, ChordStorage.Deserializer)
		}
		registerModule(module)
	}


	val writer get() = writerWithDefaultPrettyPrinter()
	val reader get() = reader()
}