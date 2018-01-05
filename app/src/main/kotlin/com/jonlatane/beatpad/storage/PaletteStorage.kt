package com.jonlatane.beatpad.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.model.melody.RationalMelody
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object PaletteStorage : AnkoLogger {
	private val gson: Gson by lazy {
		GsonBuilder()
			.registerTypeAdapter(Melement::class.java, JsonDeserializer<Melement> { melement, _, _ ->
				if (melement?.asJsonObject?.has("note") ?: false) {
					gson.fromJson(melement, Sustain::class.java)
				} else {
					gson.fromJson(melement, Note::class.java)
				}
			})
			.registerTypeAdapter(Melement::class.java, JsonSerializer<Melement> { melement, _, _ ->
				when (melement) {
					is Note -> gson.toJsonTree(melement)
					is Sustain -> gson.toJsonTree(melement)
				}
			})
			.registerTypeAdapter(Melody::class.java, JsonDeserializer<Melody> { melody, _, _ ->
				val result = gson.fromJson(melody, Melody::class.java)
				var lastNote: Note? = null
				// Update the pointers for Sustains so they point the right way.
				result.elements.indices.forEach { index ->
					val melement = result.elements[index]
					when (melement) {
						is Note -> lastNote = melement
						is Sustain -> {
							if (lastNote == null) result.elements[index] = Rest() // Invalid Sustain location
							else melement.note = lastNote ?: melement.note
						}
					}
				}
				result
			})
			.create()
	}


	fun storePalette(palette: Palette, context: Context) = try {
		val outputStreamWriter = OutputStreamWriter(context.openFileOutput("palette.json", Context.MODE_PRIVATE))
		val json = gson.toJson(palette)
		info("Stored palette: $json")
		outputStreamWriter.write(json)
		outputStreamWriter.close()
	} catch (e: IOException) {
		error("File write failed: " + e.toString())
	}

	fun loadPalette(context: Context): Palette = try {
		val json: String = InputStreamReader(context.openFileInput("palette.json")).use { it.readText() }
		info("Loaded palette: $json")
		gson.fromJson(json, Palette::class.java)!!
	} catch (t: Throwable) {
		error("Failed to load stored palette", t)
		basePalette
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
