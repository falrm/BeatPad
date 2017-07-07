package com.jonlatane.beatpad.view.tonesequence

import com.jonlatane.beatpad.R.id.play
import com.jonlatane.beatpad.harmony.ToneSequence
import com.jonlatane.beatpad.harmony.ToneSequence.Step.*
import com.jonlatane.beatpad.output.controller.DeviceOrientationInstrument
import com.jonlatane.beatpad.output.controller.SequencerThread
import com.jonlatane.beatpad.output.instrument.Instrument
import com.jonlatane.beatpad.sensors.Orientation

class ToneSequencePlayerThread(
  val instrument: Instrument,
  val viewModel: ToneSequenceViewModel,
  @Volatile var beatsPerMinute: Int
): Runnable {
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
                when(step) {
                    is Note -> {
                        instrument.stop()
                        step.tones.forEach {
                            val closestNote = viewModel.topology.chord.closestTone(it)
                            instrument.play(closestNote)
                        }
                        Thread.sleep(playDuration)
                    }
                    Rest ->  {
                        instrument.stop()
                        Thread.sleep(pauseDuration)
                    }
                }
                if (stopped) {
                    break
                }
            }
        } catch (ignored: InterruptedException) {
        }

    }
}
