package com.jonlatane.beatpad.model.melody

import com.jonlatane.beatpad.model.Melody

abstract class RecordedMIDIMelody(
	override val elements: MutableList<Melody.Element>,
	override var subdivisionsPerBeat: Int = 1,
	override var tonic: Int = 0
) : Melody {
	override var shouldConformWithHarmony = false
}