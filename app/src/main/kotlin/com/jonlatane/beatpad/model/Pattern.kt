package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.util.mod12

interface Pattern {
	val elements: MutableList<Element>
	val subdivisionsPerBeat: Int
	val relativeTo: Int
	fun transposed(newRoot: Int): List<Element> {
		return elements.map {
			it.transposed(relativeTo, newRoot)
		}
	}

	sealed class Element {
		abstract val duration: Int
		abstract fun transposed(originalRoot: Int, newRoot: Int): Element
		/**
		 * All the notes of any Pattern should be given as though the tonic center is at 0.
		 */
		class Note(
			var tones: MutableSet<Int> = mutableSetOf(),
			var velocity: Float = 1f,
		  override var duration: Int = 0
		) : Element() {
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

		class Sustain(
			val note: Note,
			override var duration: Int = 0
		) : Element() {
			override fun transposed(originalRoot: Int, newRoot: Int): Element {
				return Sustain(note.transposed(originalRoot, newRoot), duration)
			}
		}

	}
}