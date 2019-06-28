package com.jonlatane.beatpad.storage


import android.content.Context
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.storage.AppObjectMapper.reader
import com.jonlatane.beatpad.storage.AppObjectMapper.writer
import org.apache.commons.codec.binary.Base64
import org.jetbrains.anko.*
import java.io.*
import java.io.File.separator
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.reflect.KClass

interface Storage: AnkoLogger {
	val storageContext: Context

	companion object: Storage, AnkoLogger {
		override val storageContext get() = MainApplication.instance
		private const val paletteDir = "palettes"
		private const val melodyDir = "melodies"
		private const val harmonyDir = "harmonies"
		private const val openPaletteFileName = "palette.json"
		val base64 = Base64(Int.MAX_VALUE, byteArrayOf(), true)

		fun getPalettes(context: Context): List<File> {
			createDir(paletteDir, context)
			return File("${context.filesDir}$separator$paletteDir").listFiles()
				.filter { it.name.endsWith("~") }
			//.map { it.name }
		}

		private fun storePalette(palette: Palette, context: Context, file: File) = try {
			createDir(paletteDir, context)
			FileOutputStream(file.apply { createNewFile() }).use { fileOutputStream ->
				//context.openFileOutput("$paletteDir$separator$filename", Context.MODE_PRIVATE).use { fileOutputStream ->
				writer.writeValue(fileOutputStream, palette)
			}
			info {
				"Stored palette: ${stringify(palette)}"
			}
		} catch (e: IOException) {
			error("File send failed: ", e)
		}

		private fun loadPalette(context: Context, file:File): Palette {
			createDir(paletteDir, context)
			val palette = FileInputStream(file).use { fileInputStream ->
				//val palette = context.openFileInput("$paletteDir$separator$filename").use { fileInputStream ->
				AppObjectMapper.readValue(fileInputStream, Palette::class.java)
			}
			info {
				"Loaded palette: ${stringify(palette)}"
			}
			return palette
		}

		fun stringify(o: Any) = writer.writeValueAsString(o)

		private fun createDir(name: String, context: Context) {
			val dir = context.filesDir
			val dir2 = File(dir, name)
			dir2.mkdirs()
		}
	}

	fun Context.loadPalette(
		filename:String = openPaletteFileName,
		fallbackToBackup: Boolean = true
	): Palette? = try {
		loadPalette(this, File("$filesDir$separator$paletteDir$separator$filename"))
	} catch(t: Throwable) {
		error("Failed to load palette data", t)
		if(fallbackToBackup) {
			toast("Failed to load data, attempting to load backup...")
			try {
				loadPalette(this, File("$filesDir$separator$paletteDir$separator${filename}_old"))
			} catch (t: Throwable) {
				error("Failed to load any palette data, starting from scratch...", t)
				toast("Failed to load data from backup, starting from scratch...")
				null
			}
		} else null
	}

	fun Context.storePalette(palette: Palette, filename:String = openPaletteFileName) = doAsync {
		val tmpFile = File("$filesDir$separator$paletteDir${separator}tmp_palette.json")
		val destFile = File("$filesDir$separator$paletteDir$separator$filename")
		val oldFile = File("$filesDir$separator$paletteDir$separator${filename}_old")

		try { tmpFile.delete() } catch(_: Throwable) {}
		try { oldFile.delete() } catch(_: Throwable) {}
		storePalette(palette, this.weakRef.get()!!, tmpFile)
		try {
			loadPalette("tmp_palette.json", false) ?: toast("Failed to save palette!")
			try { destFile.renameTo(oldFile) || TODO("Rename returned false") } catch(t: Throwable) { warn(t) }
			try { tmpFile.renameTo(destFile) || TODO("Rename returned false") } catch(t: Throwable) { error(t); throw t }
		} catch(t: Throwable) {
			error(t)
		}
	}
	fun Palette.toURI(): URI = toURI("palette")
	fun Harmony.toURI(): URI = toURI("harmony")
	fun Melody<*>.toURI(): URI = toURI("melody")

	/**
	 * @return [null] if the schema isn't a valid BeatScratch schema; otherwise will either
	 * successfully decode the entity or throw an exception.
	 */
	fun <T: Any> URI.toEntity(entity: String, entityVersion: String, klass: KClass<out T>) : T? {
		// Validate the schema
		when(scheme) {
			"beatscratch" ->{ if (host != entity || path != "/$entityVersion") return null }
			"https" -> {
				if(
					!listOf("beatscratch.io", "api.beatscratch.io").contains(host)
					|| path != "/$entity/$entityVersion"
				) return null
			}
			else -> return null
		}
		// Read the entity
		val bytes = base64.decode(query)
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
		val encodedString = base64.encodeAsString(bytes)
		return URI("https://api.beatscratch.io/$entity/$entityVersion?$encodedString")
	}
}