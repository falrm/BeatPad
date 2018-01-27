package com.jonlatane.beatpad.storage

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.melody.RationalMelody
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter


object PaletteStorage : AnkoLogger {
	internal val mapper = ObjectMapper().apply {
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

	internal val writer get() = mapper.writerWithDefaultPrettyPrinter()
	internal val reader get() = mapper.reader()


	fun storePalette(palette: Palette, context: Context) = try {
		val outputStreamWriter = OutputStreamWriter(context.openFileOutput("palette.json", Context.MODE_PRIVATE))
		val json = stringify(palette)
		info("Stored palette: $json")
		outputStreamWriter.write(json)
		outputStreamWriter.close()
	} catch (e: IOException) {
		error("File write failed: " + e.toString())
	}

	fun loadPalette(context: Context): Palette = try {
		val json: String = InputStreamReader(context.openFileInput("palette.json")).use { it.readText() }
		info("Loaded palette: $json")
		val palette = parse(json)
		palette.normalizeDeserializedData()
		palette
	} catch (t: Throwable) {
		//error("Failed to load stored palette", t)
		basePalette
	}

	fun stringify(palette: Palette) = writer.writeValueAsString(palette)
	fun parse(data: String): Palette = mapper.readValue(data, Palette::class.java).also {
		it.normalizeDeserializedData()
	}

	private fun Palette.normalizeDeserializedData() {
		parts.forEach { part ->
			part.melodies.forEach { melody ->
				//melody.normalizeDeserializedData()
			}
		}
	}

	val basePalette
		get() = Palette().apply {
			parts.add(Part().apply {
				melodies.add(baseMelody)
			})
		}

	val baseMelody
		get() = RationalMelody(
			elements = (1..16).map { Rest() }.toMutableList(),
			subdivisionsPerBeat = 4
		)
}
