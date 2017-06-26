package com.jonlatane.beatpad.harmony

import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.util.fromOctaveToOffset

class ToneSequence(
	val steps: MutableList<ToneSequenceStep>,
  var stepsPerBeat: Int = 1,
  var octave: Int = 4
) {
	sealed class ToneSequenceStep {
		object Rest : ToneSequenceStep()
		data class Note(
			/**
			 * All the notes of any ToneSequence should be given as though the tonic center is at 0.
			 */
			val tones: MutableSet<Int>,
			var velocity: Float
		) : ToneSequenceStep()
	}

	infix fun transposeTo(c: Chord): List<ToneSequenceStep> = steps.map {
		when(it) {
			ToneSequenceStep.Rest -> ToneSequenceStep.Rest
			is ToneSequenceStep.Note -> ToneSequenceStep.Note(
				it.tones.map { c.closestTone(it + c.root) + octave.fromOctaveToOffset }.toMutableSet(),
				it.velocity
			)
		}
	}
}