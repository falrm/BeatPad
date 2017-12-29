package com.jonlatane.beatpad.model.pattern

import com.jonlatane.beatpad.model.Pattern

abstract class RecordedMIDIPattern(
	override val elements: MutableList<Pattern.Element>,
	override var subdivisionsPerBeat: Int = 1,
	override var relativeTo: Int =0
) : Pattern