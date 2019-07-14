package com.jonlatane.beatpad.storage


import android.content.Context
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Palette
import net.iharder.Base64
import org.jetbrains.anko.*
import java.io.*
import java.io.File.separator
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.reflect.KClass


interface Storage : AnkoLogger {
  val storageContext: Context
  val Context.paletteDir: String get() = "$filesDir$separator$basePaletteDir"
  val Context.melodyDir: String get() = "$filesDir$separator$baseMelodyDir"
  val Context.harmonyDir: String get() = "$filesDir$separator$baseHarmonyDir"

  companion object : Storage, AnkoLogger {
    override val storageContext get() = MainApplication.instance
    private const val basePaletteDir = "palettes"
    private const val baseMelodyDir = "melodies"
    private const val baseHarmonyDir = "harmonies"
    private const val openPaletteFileName = "palette.json"


    fun getPalettes(context: Context): List<File> {
      File(context.paletteDir).mkdirs()
      return (File(context.paletteDir).listFiles() ?: emptyArray())
        .filter { it.name.endsWith("~") }
      //.map { it.name }
    }

    private fun storePalette(palette: Palette, context: Context, file: File) = try {
      File(context.paletteDir).mkdirs()
      FileOutputStream(file.apply { createNewFile() }).use { fileOutputStream ->
        //context.openFileOutput("$paletteDir$separator$filename", Context.MODE_PRIVATE).use { fileOutputStream ->
        AppObjectMapper.writeValue(fileOutputStream, palette)
      }
      info {
        "Stored palette: ${stringify(palette)}"
      }
    } catch (e: IOException) {
      error("File send failed: ", e)
    }

    private fun loadPalette(context: Context, file: File): Palette {
      File(context.paletteDir).mkdirs()
      val palette = FileInputStream(file).use { fileInputStream ->
        //val palette = context.openFileInput("$paletteDir$separator$filename").use { fileInputStream ->
        AppObjectMapper.readValue(fileInputStream, Palette::class.java)
      }
      info {
        "Loaded palette: ${stringify(palette)}"
      }
      return palette
    }

    fun stringify(o: Any): String = AppObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o)
  }

  val File.newTmpVersion get() = File("$path.tmp")
  val File.backup: File get() = File("$path.backup")

  fun Context.loadPalette(
    file: File,
    fallbackToBackup: Boolean = true
  ): Palette? = try {
    loadPalette(this, file)
  } catch (t: Throwable) {
    error("Failed to load palette data", t)
    if (fallbackToBackup) {
      toast("Failed to load data, attempting to load backup...")
      try {
        loadPalette(this, file.backup)
      } catch (t: Throwable) {
        error("Failed to load any palette data, starting from scratch...", t)
        toast("Failed to load data from backup, starting from scratch...")
        null
      }
    } else null
  }

  fun Context.loadPalette(
    filename: String = openPaletteFileName,
    fallbackToBackup: Boolean = true
  ): Palette? = loadPalette(
    File("$paletteDir$separator$filename"),
    fallbackToBackup
  )

  fun Context.storePalette(palette: Palette, filename: String = openPaletteFileName) = storePalette(
    palette, File("$paletteDir$separator$filename")
  )

  fun Context.storePalette(palette: Palette, file: File) = doAsync {
    val tmpFile = file.newTmpVersion
    val backupFile = file.backup

    var success = false
    var retriesRemaining = 5
    while (!success && retriesRemaining > 0) {
      try {
        tmpFile.delete()
      } catch (_: Throwable) {
      }
      try {
        storePalette(palette, this.weakRef.get()!!, tmpFile)
        success = loadPalette(tmpFile, false) != null
        try {
          backupFile.delete()
        } catch (_: Throwable) {
        }
        try {
          file.renameTo(backupFile) || TODO("Rename returned false")
        } catch (t: Throwable) {
          warn(t)
        }
        try {
          tmpFile.renameTo(file) || TODO("Rename returned false")
        } catch (t: Throwable) {
          error(t); throw t
        }
      } catch (t: Throwable) {
        error(t)
      }
      retriesRemaining--
    }

    if (!success) {
      uiThread { toast("Failed to save palette!") }
    }
  }

  fun Palette.toURI(): URI = toURI("palette")
  fun Harmony.toURI(): URI = toURI("harmony")
  fun Melody<*>.toURI(): URI = toURI("melody")

  /**
   * @return [null] if the schema isn't a valid BeatScratch schema; otherwise will either
   * successfully decode the entity or throw an exception.
   */
  fun <T : Any> URI.toEntity(entity: String, entityVersion: String, klass: KClass<out T>): T? {
    // Validate the schema
    when (scheme) {
      "beatscratch" -> {
        if (host != entity || path != "/$entityVersion") return null
      }
      "https"       -> {
        if (
          !listOf("beatscratch.io", "api.beatscratch.io").contains(host)
          || path != "/$entity/$entityVersion"
        ) return null
      }
      else          -> return null
    }
    // Read the entity
    val bytes = Base64.decode(query)
    return ZipInputStream(ByteArrayInputStream(bytes)).use { zipInputStream ->
      zipInputStream.nextEntry
      AppObjectMapper.readValue(zipInputStream, klass.java)
    }
  }

  fun Any.toURI(entity: String, entityVersion: String = "v1"): URI {
    val bytes = ByteArrayOutputStream().use { bytes ->
      ZipOutputStream(bytes).use { out ->
        out.putNextEntry(ZipEntry("object.json"))
        AppObjectMapper.writer().writeValue(out, this)
      }
      bytes.toByteArray()
    }
    val encodedString = Base64.encodeBytes(bytes)
    return URI("https://api.beatscratch.io/$entity/$entityVersion?$encodedString")
  }
}