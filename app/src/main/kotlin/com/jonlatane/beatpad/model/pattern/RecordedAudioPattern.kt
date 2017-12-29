package com.jonlatane.beatpad.model.pattern

import com.jonlatane.beatpad.model.Pattern

abstract class RecordedAudioPattern(
	override val subdivisions: MutableList<Pattern.Subdivision>,
	override var subdivisionsPerBeat: Int = 1,
	override var relativeTo: Int =0
) : Pattern