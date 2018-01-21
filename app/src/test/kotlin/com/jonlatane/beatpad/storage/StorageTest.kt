package com.jonlatane.beatpad.storage

import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.harmony.chord.*
import com.jonlatane.beatpad.view.colorboard.BaseColorboardView
import io.damo.aspen.*
import org.assertj.core.api.Assertions.*

class StorageTest : Test({
	val palette = PaletteStorage.basePalette
	test("Storage") {
		val data = PaletteStorage.stringify(palette)
		println(data)
		val newPalette = PaletteStorage.parse(data)
		println(PaletteStorage.stringify(newPalette))
	}
})