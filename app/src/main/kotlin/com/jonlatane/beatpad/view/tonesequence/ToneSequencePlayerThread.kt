package com.jonlatane.beatpad.view.tonesequence

import com.jonlatane.beatpad.harmony.Rest
import com.jonlatane.beatpad.harmony.ToneSequence.Step.Note
import com.jonlatane.beatpad.output.instrument.Instrument

class ToneSequencePlayerThread(
	val instrument: Instrument,
	val viewModel: ToneSequenceViewModel,
	@Volatile var beatsPerMinute: Int
) : Runnable {
	@Volatile var stopped = false


	override fun run() {
		while (!stopped) {
			playBeat()
		}
	}

	private fun playBeat() {
		try {

			for (step in viewModel.toneSequence.steps) {
				val playDuration = 60000L / (beatsPerMinute * viewModel.toneSequence.stepsPerBeat)
				val pauseDuration = 0L

				// Interpret the booleans as "play" or "rest"
				when (step) {
					is Note -> {
						instrument.stop()
						step.tones.forEach {
							val closestNote = viewModel.topology.chord.closestTone(it)
							instrument.play(closestNote)
						}
						Thread.sleep(playDuration)
					}
				}
				if (stopped) {
					instrument.stop()
					break
				}
			}
		} catch (ignored: InterruptedException) {
		}

	}
}
