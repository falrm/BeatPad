package com.jonlatane.beatpad.storage

import com.jonlatane.beatpad.model.Melody
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
	}
})