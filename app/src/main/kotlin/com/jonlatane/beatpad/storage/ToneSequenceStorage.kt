package com.jonlatane.beatpad.storage

import android.content.Context
import android.util.Log
import com.google.gson.*
import com.jonlatane.beatpad.harmony.*
import com.jonlatane.beatpad.harmony.ToneSequence.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import java.io.*
import com.jonlatane.beatpad.harmony.ToneSequence
import org.jetbrains.anko.info
import java.lang.reflect.Type

object ToneSequenceStorage : AnkoLogger {
	private val gson: Gson by lazy {
		GsonBuilder()
			.registerTypeAdapter(Step::class.java, JsonDeserializer<Step> { element, _, _ ->
				if (element?.asJsonObject?.has("note") ?: false) {
					gson.fromJson(element, Sustain::class.java)
				} else {
					gson.fromJson(element, Note::class.java)
				}
			})
			.registerTypeAdapter(Step::class.java, JsonSerializer { step: Step, _, _ ->
				when(step) {
					is Note -> gson.toJsonTree(step as Note)
					is Sustain -> gson.toJsonTree(step as Sustain)
				}
			})
			.create()
	}


	fun storeSequence(toneSequence: ToneSequence, context: Context) = try {
		val outputStreamWriter = OutputStreamWriter(context.openFileOutput("sequence.json", Context.MODE_PRIVATE))
		val json = gson.toJson(toneSequence)
		info("Stored ToneSequence: $json")
		outputStreamWriter.write(json)
		outputStreamWriter.close()
	} catch (e: IOException) {
		Log.e("Exception", "File write failed: " + e.toString());
	}

	fun loadSequence(context: Context): ToneSequence = try {
		val json: String = InputStreamReader(context.openFileInput("sequence.json")).use { it.readText() }
		info("Loaded ToneSequence: $json")
		gson.fromJson(json, ToneSequence::class.java)!!
	} catch (t: Throwable) {
		error("Failed to load stored sequence", t)
		defaultSequence
	}

	val defaultSequence = ToneSequence(
		listOf(
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest()
		),
		stepsPerBeat = 4
	)
}
