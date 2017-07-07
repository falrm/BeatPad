package com.jonlatane.beatpad.harmony

class ToneSequence(
	steps: List<Step> = emptyList(),
	/**
	 * A value of 4 would indicate sixteenth notes in 4/4 time
	 */
	var stepsPerBeat: Int = 1
) {
	val steps = steps.toMutableList()
	sealed class Step {
		object Rest: Step()
		/**
		 * All the notes of any ToneSequence should be given as though the tonic center is at 0.
		 */
		data class Note(
			var tones: Set<Int>,
			var velocity: Float = 1f
		): Step()
		data class Sustain(
			val note: Note
		): Step()
	}
}