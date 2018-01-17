package com.jonlatane.beatpad.storage

import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.harmony.chord.*
import com.jonlatane.beatpad.view.colorboard.BaseColorboardView
import io.damo.aspen.*
import org.assertj.core.api.Assertions.*
import kotlinx.serialization.*
import kotlinx.serialization.cbor.CBOR
import kotlinx.serialization.json.JSON

class StorageTest : Test({
	val palette = Palette()
	test("Storage") {
		val data = JSON.stringify(palette)
		println(data)
	}
})

object PaletteStorageYay {
  val json = JSON(
	  indent = "  "
  )
	fun store(file: String, palette: Palette) {

	}
}