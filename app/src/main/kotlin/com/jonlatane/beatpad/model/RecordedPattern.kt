package com.jonlatane.beatpad.model

class RecordedPattern(
	override val subdivisions: MutableList<Pattern.Subdivision>,
	override var subdivisionsPerBeat: Int = 1,
	override var relativeTo: Int =0
) : Pattern