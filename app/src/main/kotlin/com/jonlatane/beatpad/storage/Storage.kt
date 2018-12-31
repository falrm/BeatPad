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
import org.jetbrains.anko.verbose
import java.io.*
import java.io.File.separator

interface Storage: AnkoLogger {
	fun Context.loadPalette(filename:String = openPaletteFileName) = loadPalette(this, filename)
	fun Context.storePalette(palette: Palette, filename:String = openPaletteFileName)
	  = storePalette(palette, this, filename)

  companion object: AnkoLogger {
		private const val paletteDir = "palettes"
		private const val melodyDir = "melodies"
		private const val harmonyDir = "harmonies"
		private const val openPaletteFileName = "palette.json"

		private fun createDir(name: String, context: Context) {
			val dir = context.filesDir
			val dir2 = File(dir, name)
			dir2.mkdirs()
		}

		fun getPalettes(context: Context): List<String> {
			createDir(paletteDir, context)
			return File("${context.filesDir}$separator$paletteDir").listFiles()
				.filter { it.name.endsWith("~") }
				.map { it.name }
		}

		fun storePalette(palette: Palette, context: Context, filename:String = openPaletteFileName) = try {
			createDir(paletteDir, context)
      FileOutputStream(
        File("${context.filesDir}$separator$paletteDir$separator$filename").apply { createNewFile() }
      ).use { fileOutputStream ->
			//context.openFileOutput("$paletteDir$separator$filename", Context.MODE_PRIVATE).use { fileOutputStream ->
				writer.writeValue(fileOutputStream, palette)
			}
			info {
				"Stored palette: ${stringify(palette)}"
			}
		} catch (e: IOException) {
			error("File send failed: ", e)
		}

		fun loadPalette(context: Context, filename:String = openPaletteFileName): Palette = try {
			createDir(paletteDir, context)
      val palette = FileInputStream(
        File("${context.filesDir}$separator$paletteDir$separator$filename")
      ).use { fileInputStream ->
      //val palette = context.openFileInput("$paletteDir$separator$filename").use { fileInputStream ->
				AppObjectMapper.readValue(fileInputStream, Palette::class.java)
			}
			info {
				"Loaded palette: ${stringify(palette)}"
			}
			palette
		} catch (t: Throwable) {
			error("Failed to load stored palette", t)
			//temporary: fallback to not the palettes directory
			try {
				val palette = context.openFileInput(filename).use { fileInputStream ->
					AppObjectMapper.readValue(fileInputStream, Palette::class.java)
				}
				info {
					"Loaded palette: ${stringify(palette)}"
				}
				palette
			} catch (t: Throwable) {
				error("Failed to load stored palette", t)
				PaletteStorage.basePalette
			}
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

		fun stringify(o: Any) = writer.writeValueAsString(o)
	}
}