package com.jonlatane.beatpad.storage

import android.content.Context
import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.model.melody.RationalMelody
import kotlinx.serialization.json.JSON
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object PaletteStorage : AnkoLogger {


	fun storePalette(palette: Palette, context: Context) = try {
		val outputStreamWriter = OutputStreamWriter(context.openFileOutput("palette.json", Context.MODE_PRIVATE))
		val json = JSON.stringify(palette)
		info("Stored palette: $json")
		outputStreamWriter.write(json)
		outputStreamWriter.close()
	} catch (e: IOException) {
		error("File write failed: " + e.toString())
	}

	fun loadPalette(context: Context): Palette = try {
		val json: String = InputStreamReader(context.openFileInput("palette.json")).use { it.readText() }
		info("Loaded palette: $json")
		val palette = JSON.parse<Palette>(json)
		palette.normalizeDeserializedData()
		palette
	} catch (t: Throwable) {
		error("Failed to load stored palette", t)
		basePalette
	}

	private fun Palette.normalizeDeserializedData() {
		parts.forEach {  part ->
			part.segments.forEach { melody ->
				melody.normalizeDeserializedData()
			}
		}
	}

	private fun Melody.normalizeDeserializedData() {
		var lastNote: Note? = null
		elements.indices.forEach { index ->
			val melement = elements[index]
			when (melement) {
				is Note -> lastNote = melement
				is Sustain -> {
					if (lastNote == null) elements[index] = Rest() // Invalid Sustain location
					else melement.note = lastNote ?: melement.note
				}
			}
		}
	}

	val basePalette
		get() = Palette().apply {
			parts.add(Part().apply {
				segments.add(baseMelody)
			})
		}

	val baseMelody
		get() = RationalMelody(
			elements = (1..16).map { Rest() },
			subdivisionsPerBeat = 4
		)
}
