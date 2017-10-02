package com.jonlatane.beatpad.output.controller

import com.jonlatane.beatpad.model.HyperSequence
import com.jonlatane.beatpad.output.instrument.Instrument
import java.util.concurrent.Executors
import kotlin.properties.Delegates.observable

class HyperSequencePlayerThread(
	val instrument: Instrument,
	val sequence: HyperSequence,
	@Volatile var beatsPerMinute: Int
) : Runnable {
  private val executorService = Executors.newScheduledThreadPool(16)
  private var toneSequenceThreads = mutableListOf<ToneSequencePlayerThread>()
	var stopped by observable(false, { _, _, value ->
    toneSequenceThreads.forEach { it.stopped = value }
  })

	override fun run() {
		while (!stopped) {
			playBeat()
		}
	}

	private fun playBeat() {
		try {
			for((chord, playback) in sequence.deltas) {
        for((instrument, toneSequence) in playback) {
          val thread = ToneSequencePlayerThread(
            instrument = instrument,
            sequence = toneSequence,
            chordResolver = { chord },
            onFinish = { toneSequenceThreads.remove(it) },
            beatsPerMinute = beatsPerMinute
          )
          toneSequenceThreads.add(thread)
          executorService.execute(thread)
        }
        if (stopped) {
          instrument.stop()
        }
			}
		} catch (ignored: InterruptedException) {
		}

	}
}
