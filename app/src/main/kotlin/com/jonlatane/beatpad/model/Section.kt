package com.jonlatane.beatpad.model

import android.util.Rational
import com.jonlatane.beatpad.harmony.chord.Chord

/**
 *
 */
class Section {
	data class Chunk(
		val duration: Rational = Rational(1,1),
		val chord: Chord
	)

	val chunks = mutableListOf<Chunk>()
}