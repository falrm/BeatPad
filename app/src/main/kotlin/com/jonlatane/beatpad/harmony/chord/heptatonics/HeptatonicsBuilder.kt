package com.jonlatane.beatpad.harmony.chord.heptatonics

import com.jonlatane.beatpad.harmony.chord.*

object HeptatonicsBuilder {
	fun build(
		root: Int,
		two: Int,
		three: Int,
		four: Int,
		five: Int,
		six: Int,
		seven: Int
	) = Chord(
		root = root,
		extension = mapOf(
			2 to two,
			4 to three,
			5 to four,
			7 to five,
			9 to six,
			11 to seven
		).mapNotNull {
			when (it.value) {
				DIMINISHED, MINOR -> it.key - 1
				PERFECT, MAJOR -> it.key - 1
				AUGMENTED -> it.key + 1
				NONEXISTENT -> null
				else -> throw Exception("This should be impossible.")
			}
		}.toIntArray()
	)
}