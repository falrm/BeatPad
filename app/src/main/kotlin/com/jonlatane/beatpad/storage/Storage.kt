package com.jonlatane.beatpad.storage

import android.content.Context
import android.util.Base64
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.storage.AppObjectMapper.reader
import com.jonlatane.beatpad.storage.AppObjectMapper.writer
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import java.io.*
import java.io.File.separator
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.reflect.KClass

interface Storage: AnkoLogger {
	val storageContext: Context

	fun Context.loadPalette(filename:String = openPaletteFileName) = loadPalette(this, filename)
	fun Context.storePalette(palette: Palette, filename:String = openPaletteFileName)
	  = storePalette(palette, this, filename)

  companion object: AnkoLogger {
		private const val paletteDir = "palettes"
		private const val melodyDir = "melodies"
		private const val harmonyDir = "harmonies"
		private const val openPaletteFileName = "palette.json"

		fun getPalettes(context: Context): List<File> {
			createDir(paletteDir, context)
			return File("${context.filesDir}$separator$paletteDir").listFiles()
				.filter { it.name.endsWith("~") }
				//.map { it.name }
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

		fun stringify(o: Any) = writer.writeValueAsString(o)

		private fun createDir(name: String, context: Context) {
			val dir = context.filesDir
			val dir2 = File(dir, name)
			dir2.mkdirs()
		}
	}
	fun Palette.toURI(): URI = toURI("palette")
	fun Harmony.toURI(): URI = toURI("harmony")
	fun Melody<*>.toURI(): URI = toURI("melody")

	fun <T: Any> URI.toEntity(entity: String, entityVersion: String, klass: KClass<out T>) : T? {
		if(scheme != "beatscratch" || host != entity || path != "/$entityVersion") return null
		val bytes = Base64.decode(query, Base64.NO_WRAP)
		return ZipInputStream(ByteArrayInputStream(bytes)).use { zipInputStream ->
			zipInputStream.nextEntry
			AppObjectMapper.readValue(zipInputStream, klass.java)
		}
	}

	fun Any.toURI(entity: String, entityVersion: String = "v1"): URI {
		val bytes = ByteArrayOutputStream().use { bytes ->
			ZipOutputStream(bytes).use { out ->
				out.putNextEntry(ZipEntry("object.json"))
				writer.writeValue(out, this)
			}
			bytes.toByteArray()
		}
		val encodedString = Base64.encodeToString(bytes, Base64.NO_WRAP)
		return URI("beatscratch://$entity/$entityVersion?$encodedString")
	}
}