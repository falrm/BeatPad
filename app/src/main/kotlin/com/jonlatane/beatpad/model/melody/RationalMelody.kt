package com.jonlatane.beatpad.model.melody

import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Melody.Element

class RationalMelody(
	elements: List<Element> = emptyList(),
	/** A value of 4 would indicate sixteenth notes in 4/4 time */
	override var subdivisionsPerBeat: Int = 1
) : Melody {
	override var shouldConformWithHarmony = false
	override val elements: MutableList<Element>
		= elements.toMutableList()
	override var tonic: Int = 0
	override var enabled = true
	override val type get() = "rational"
}