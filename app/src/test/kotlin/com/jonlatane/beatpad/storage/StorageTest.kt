package com.jonlatane.beatpad.storage

import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Palette
import io.damo.aspen.Test
import org.assertj.core.api.Assertions.assertThat

class StorageTest : Test({
	with(Storage) {
		test("Melody URI Storage") {
			val data = PaletteStorage.baseMelody.toURI()
			val actual: Melody<*> = data.toEntity("melody", "v1", Melody::class)!!
			val expected = PaletteStorage.baseMelody
			assertThat(actual.length == expected.length)
			assertThat(actual.subdivisionsPerBeat == expected.subdivisionsPerBeat)
		}
		test("Harmony URI Storage") {
			val data = PaletteStorage.baseHarmony.toURI()
			val actual: Harmony = data.toEntity("harmony", "v1", Harmony::class)!!
			val expected = PaletteStorage.baseHarmony
			assertThat(actual.length == expected.length)
			assertThat(actual.subdivisionsPerBeat == expected.subdivisionsPerBeat)
		}
		test("Palette URI Storage") {
			val data = PaletteStorage.basePalette.toURI()
			val actual: Palette = data.toEntity("palette", "v1", Palette::class)!!
			val expected = PaletteStorage.basePalette
			assertThat(actual.parts.size == expected.parts.size)
			assertThat(actual.sections.size == expected.sections.size)
		}
	}
})