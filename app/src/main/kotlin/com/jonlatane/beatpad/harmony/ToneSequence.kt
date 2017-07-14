package com.jonlatane.beatpad.harmony

import com.jonlatane.beatpad.util.mod12

class ToneSequence(
	steps: List<Step> = emptyList(),
	/** A value of 4 would indicate sixteenth notes in 4/4 time */
	var stepsPerBeat: Int = 1,
	var preferredRoot: Int = 0
) {
	var steps: MutableList<Step> = steps.toMutableList()
	fun transposed(newRoot: Int): List<Step> {
		return steps.map { it.transposed(preferredRoot, newRoot) }
	}

	sealed class Step {
		/**
		 * All the notes of any ToneSequence should be given as though the tonic center is at 0.
		 */
		data class Note(
			var tones: MutableSet<Int> = mutableSetOf(),
			var velocity: Float = 1f
		) : Step() {
			/**
			 *
			 */
			override fun transposed(originalRoot: Int, newRoot: Int): Note {
				val nearestNewRootToOriginal: Int = ((originalRoot - 11)..(originalRoot + 11))
					.filter { it.mod12 == newRoot.mod12 }
					.minBy { Math.abs(it - originalRoot) - 1f/it }!! // the 1f/it factor will make the lower one
				val difference = nearestNewRootToOriginal - originalRoot
				return Note(
					tones = tones.map { it + difference}.toMutableSet(),
					velocity = velocity
				)
			}
		}

		data class Sustain(
			val note: Note
		) : Step() {
			override fun transposed(originalRoot: Int, newRoot: Int): Step {
				return Sustain(note.transposed(originalRoot, newRoot))
			}
		}


		abstract fun transposed(originalRoot: Int, newRoot: Int): Step

	}
}

typealias Rest = ToneSequence.Step.Note
typealias Note = ToneSequence.Step.Note
typealias Sustain = ToneSequence.Step.Sustain