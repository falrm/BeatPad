package com.jonlatane.beatpad.storage

import android.content.Context
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.Rest
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.storage.AppObjectMapper.writer
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter


object PaletteStorage : AnkoLogger {


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
		palette
	} catch (t: Throwable) {
		//error("Failed to load stored palette", t)
		basePalette
	}

	fun stringify(palette: Palette) = writer.writeValueAsString(palette)
	fun parse(data: String): Palette = AppObjectMapper.readValue(data, Palette::class.java)

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
