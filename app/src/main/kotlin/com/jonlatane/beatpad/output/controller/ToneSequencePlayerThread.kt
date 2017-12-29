package com.jonlatane.beatpad.output.controller

import com.jonlatane.beatpad.model.Pattern
import com.jonlatane.beatpad.model.Pattern.Subdivision.Note
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.Instrument
import com.jonlatane.beatpad.view.tonesequence.PatternViewModel

class ToneSequencePlayerThread(
	val instrument: Instrument,
	val viewModel: PatternViewModel? = null,
	val sequence: Pattern = viewModel!!.toneSequence,
	val chordResolver: () -> Chord = { viewModel!!.orbifold.chord },
	val onFinish: (ToneSequencePlayerThread) -> Unit = {},
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

			for (step in sequence.subdivisions) {
				val playDuration = 60000L / (beatsPerMinute * sequence.subdivisionsPerBeat)
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
		} finally {
		  onFinish(this)
		}

	}
}
