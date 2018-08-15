package com.jonlatane.beatpad.storage

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.model.harmony.chord.Chord

object AppObjectMapper : ObjectMapper() {
	init {
		val module = SimpleModule().apply {
      //addSerializer(Harmony::class.java, HarmonyStorage.Serializer)
      //addDeserializer(Harmony::class.java, HarmonyStorage.Deserializer)

			addSerializer(Melody::class.java, MelodyStorage.Serializer)
			//addDeserializer(MelodyElement::class.java, MelodyStorage.ElementDeserializer)
			addDeserializer(Melody::class.java, MelodyStorage.Deserializer)

			addDeserializer(Instrument::class.java, InstrumentStorage.Deserializer)

			addSerializer(Chord::class.java, ChordStorage.Serializer)
			addDeserializer(Chord::class.java, ChordStorage.Deserializer)

			addSerializer(Palette::class.java, PaletteStorage.Serializer)
			addDeserializer(Palette::class.java, PaletteStorage.Deserializer)
		}
		registerModule(module)
		registerKotlinModule()
	}


	val writer get() = writerWithDefaultPrettyPrinter()
	val reader get() = reader()
}