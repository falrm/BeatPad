package com.jonlatane.beatpad.storage

import android.content.Context
import com.jonlatane.beatpad.midi.GM1Effects
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object InstrumentSelectionStorage: AnkoLogger {
  fun storeGM1InstrumentSelection(instrument: Int, context: Context) = try {
    val currentChoices = loadGM1InstrumentRecents(context)
    val newVersion = (currentChoices - instrument).toMutableList().apply {
      add(0, instrument)
    }
    val outputStreamWriter = OutputStreamWriter(context.openFileOutput("gm1_choices.json", Context.MODE_PRIVATE))
    val json = Storage.stringify(newVersion)
    info("Stored GM1 instrument choices: $json")
    outputStreamWriter.write(json)
    outputStreamWriter.close()
  } catch (e: IOException) {
    error("File send failed", e)
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
}