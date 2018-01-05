package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord

class Harmony(
	elements: List<Chord> = emptyList(),
	override var subdivisionsPerBeat: Int = 1
) : Pattern<Chord> {
	override val elements: MutableList<Chord> = elements.toMutableList()
	override var tonic: Int = 0
	override fun transpose(interval: Int) = Harmony(
		elements.map { it.transpose(interval) },
		subdivisionsPerBeat
	)
}