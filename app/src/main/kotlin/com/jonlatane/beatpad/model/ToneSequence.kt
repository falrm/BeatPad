package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.util.mod12
import kotlin.properties.Delegates

interface ToneSequence {
	val subdivisions: MutableList<Subdivision>
	val subdivisionsPerBeat: Int
	val relativeTo: Int
	fun transposed(newRoot: Int): List<Subdivision> {
		return subdivisions.map {
			it.transposed(relativeTo, newRoot)
		}
	}

	sealed class Subdivision {
		abstract val duration: Int
		abstract fun transposed(originalRoot: Int, newRoot: Int): Subdivision
		/**
		 * All the notes of any ToneSequence should be given as though the tonic center is at 0.
		 */
		data class Note(
			var tones: MutableSet<Int> = mutableSetOf(),
			var velocity: Float = 1f,
		  override var duration: Int = 0
		) : Subdivision() {
			/**
			 *
			 */
			override fun transposed(originalRoot: Int, newRoot: Int): Note {
				val nearestNewRootToOriginal: Int
					= ((originalRoot - 11)..(originalRoot + 11))
					.filter { it.mod12 == newRoot.mod12 }
					.minBy { Math.abs(it - originalRoot) - 1f/it }!! // the 1f/it factor will make the lower one
				val difference = nearestNewRootToOriginal - originalRoot
				return Note(
					tones = tones.map { it + difference }.toMutableSet(),
					velocity = velocity,
					duration = duration
				)
			}
		}

		data class Sustain(
			val note: Note,
			override var duration: Int = 0
		) : Subdivision() {
			override fun transposed(originalRoot: Int, newRoot: Int): Subdivision {
				return Sustain(note.transposed(originalRoot, newRoot), duration)
			}
		}

	}
}