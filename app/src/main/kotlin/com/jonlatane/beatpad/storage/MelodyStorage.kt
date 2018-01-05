package com.jonlatane.beatpad.storage

import android.content.Context
import android.util.Log
import com.google.gson.*
import com.jonlatane.beatpad.model.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import java.io.*
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.melody.RationalMelody
import org.jetbrains.anko.info

object MelodyStorage : AnkoLogger {
	private val gson: Gson by lazy {
		GsonBuilder()
			.registerTypeAdapter(Melement::class.java, JsonDeserializer<Melement> { element, _, _ ->
				if (element?.asJsonObject?.has("note") ?: false) {
					gson.fromJson(element, Sustain::class.java)
				} else {
					gson.fromJson(element, Note::class.java)
				}
			})
			.registerTypeAdapter(Melement::class.java, JsonSerializer { step: Melement, _, _ ->
				when(step) {
					is Note -> gson.toJsonTree(step as Note)
					is Sustain -> gson.toJsonTree(step as Sustain)
				}
			})
			.create()
	}


	fun storeSequence(melody: Melody, context: Context) = try {
		val outputStreamWriter = OutputStreamWriter(context.openFileOutput("sequence.json", Context.MODE_PRIVATE))
		val json = gson.toJson(melody)
		info("Stored Melody: $json")
		outputStreamWriter.write(json)
		outputStreamWriter.close()
	} catch (e: IOException) {
		Log.e("Exception", "File write failed: " + e.toString());
	}

	fun loadSequence(context: Context): Melody = try {
		val json: String = InputStreamReader(context.openFileInput("sequence.json")).use { it.readText() }
		info("Loaded Melody: $json")
		gson.fromJson(json, Melody::class.java)!!
	} catch (t: Throwable) {
		error("Failed to load stored sequence", t)
		PaletteStorage.baseMelody
	}
}
