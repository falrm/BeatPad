package com.jonlatane.beatpad.view.tonesequence

import com.jonlatane.beatpad.model.ToneSequence
import com.jonlatane.beatpad.model.ToneSequence.Step.Note
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.Instrument

class ToneSequencePlayerThread(
	val instrument: Instrument,
	val viewModel: ToneSequenceViewModel? = null,
	val sequenceResolver: () -> ToneSequence = { viewModel!!.toneSequence },
	val chordResolver: () -> Chord = { viewModel!!.orbifold.chord },
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

			for (step in sequenceResolver().steps) {
				val playDuration = 60000L / (beatsPerMinute * sequenceResolver().stepsPerBeat)
				val pauseDuration = 0L

				println("playing")
				viewModel?.orbifold?.post { viewModel.markPlaying(step) }
				// Interpret the booleans as "play" or "rest"
				when (step) {
					is Note -> {
						print("Note: ")
						instrument.stop()
						step.tones.forEach {
							val closestNote = chordResolver().closestTone(it)

							println("playing $closestNote")
							instrument.play(closestNote)
						}
						Thread.sleep(playDuration)
					}
					else -> print("Sustain: ")
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
