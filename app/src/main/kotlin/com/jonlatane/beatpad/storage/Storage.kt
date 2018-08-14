package com.jonlatane.beatpad.storage

import android.content.Context
import android.util.Log
import com.jonlatane.beatpad.midi.GM1Effects
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.storage.AppObjectMapper.writer
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object Storage: AnkoLogger {


	fun storePalette(palette: Palette, context: Context) = try {
		val outputStreamWriter = OutputStreamWriter(context.openFileOutput("palette.json", Context.MODE_PRIVATE))
		val json = stringify(palette)
		info("Stored palette: $json")
		outputStreamWriter.write(json)
		outputStreamWriter.close()
	} catch (e: IOException) {
		error("File send failed: " + e.toString())
	}

	fun loadPalette(context: Context): Palette = try {
		val json: String = InputStreamReader(context.openFileInput("palette.json")).use { it.readText() }
		info("Loading palette: $json")
		info("Json last char: ${json[json.length - 1]}")
		val palette = AppObjectMapper.readValue(json, Palette::class.java)
		info("Loaded palette!")
		palette
	} catch (t: Throwable) {
		error("Failed to load stored palette", t)
		PaletteStorage.basePalette
	}

	fun storeGM1InstrumentSelection(instrument: Int, context: Context) = try {
		val currentChoices = loadGM1InstrumentRecents(context)
		val newVersion = (currentChoices - instrument).toMutableList().apply {
			add(0, instrument)
		}
		val outputStreamWriter = OutputStreamWriter(context.openFileOutput("gm1_choices.json", Context.MODE_PRIVATE))
		val json = stringify(newVersion)
		info("Stored GM1 instrument choices: $json")
		outputStreamWriter.write(json)
		outputStreamWriter.close()
	} catch (e: IOException) {
		error("File send failed: " + e.toString())
	}

	fun loadGM1InstrumentRecents(context: Context): List<Int> = try {
		val json: String = InputStreamReader(context.openFileInput("gm1_choices.json")).use { it.readText() }
		info("Loaded GM1 instrument choices: $json")
		val data = AppObjectMapper.readValue(json, IntArray::class.java).toList()
		data
	} catch (t: Throwable) {
		//error("Failed to load stored palette", t)
		GM1Effects.MIDI_INSTRUMENT_NAMES.indices.toList()
	}


	fun storeSequence(melody: Melody, context: Context) = try {
		val outputStreamWriter = OutputStreamWriter(context.openFileOutput("sequence.json", Context.MODE_PRIVATE))
		val json = AppObjectMapper.writeValueAsString(melody)
		info("Stored Melody: $json")
		outputStreamWriter.write(json)
		outputStreamWriter.close()
	} catch (e: IOException) {
		Log.e("Exception", "File send failed: " + e.toString())
	}

	fun loadSequence(context: Context): Melody = try {
		val json: String = InputStreamReader(context.openFileInput("sequence.json")).use { it.readText() }
		info("Loaded Melody: $json")
		AppObjectMapper.readValue(json, Melody::class.java)
	} catch (t: Throwable) {
		error("Failed to load stored sequence", t)
		PaletteStorage.baseMelody
	}

	fun stringify(o: Any) = writer.writeValueAsString(o)
}