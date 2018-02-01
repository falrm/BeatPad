package com.jonlatane.beatpad.storage

import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.Rest
import com.jonlatane.beatpad.model.melody.RationalMelody
import org.jetbrains.anko.AnkoLogger


object PaletteStorage : AnkoLogger {

	val basePalette
		get() = Palette().apply {
			parts.add(Part().apply {
				melodies.add(baseMelody)
			})
		}

	val baseMelody
		get() = RationalMelody(
			elements = (1..16).map { Rest() }.toMutableList(),
			subdivisionsPerBeat = 4
		)
}
