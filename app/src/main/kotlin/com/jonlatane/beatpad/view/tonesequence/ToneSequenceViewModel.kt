package com.jonlatane.beatpad.view.tonesequence

import com.jonlatane.beatpad.harmony.ToneSequence
import com.jonlatane.beatpad.util.times

class ToneSequenceViewModel(
	val toneSequence: ToneSequence = ToneSequence(
		listOf(
			ToneSequence.Step.Note(setOf(0, 4, 7))
		) * 8,
		stepsPerBeat = 4
	)
) {
	var viewCount = toneSequence.steps.count()
		private set
}