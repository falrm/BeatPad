package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.util.mod12

interface Melody: Pattern<Melody.Element> {
	var shouldConformWithHarmony: Boolean
	var enabled: Boolean
	var velocityFactor: Float
	val type get() = "base"

	override fun transpose(interval: Int): Melody {
		return RationalMelody(
			elements.map {
				it.transpose(interval)
			}
		)
	}

	fun offsetUnder(chord: Chord) = when {
		shouldConformWithHarmony -> {
			chord.root.mod12.let {root ->
				when {
					root > 6 -> root - 12
					else -> root
				}
			}
		}
		else -> 0
	}

	sealed class Element: Transposable<Element> {
		/**
		 * All the notes of any Melody should be given as though the tonic center is at 0.
		 */
		class Note(
			var tones: MutableSet<Int> = mutableSetOf(),
			var velocity: Float = 1f
		) : Element() {
			override fun transpose(interval: Int): Note {
				return Note(
					tones = tones.map { it + interval }.toMutableSet(),
					velocity = velocity
				)
			}
			fun offsetUnder(chord: Chord, melody: Melody) = when {
				melody.shouldConformWithHarmony -> {
					chord.root.mod12.let {root ->
						when {
							root > 6 -> root - 12
							else -> root
						}
					}
				}
				else -> 0
			}
			val isRest get() = tones.isEmpty()
		}

		class Sustain(
			var note: Note
		) : Element() {
			override fun transpose(interval: Int): Element {
				return Sustain(note.transpose(interval))
			}
		}

	}
}