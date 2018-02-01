package com.jonlatane.beatpad.output.service

import BeatClockPaletteConsumer
import android.util.Log
import org.jetbrains.anko.AnkoLogger
import java.util.concurrent.Executors

object BeatClockProducer : AnkoLogger {
	var bpm: Int = 120
	private const val subdivisionsPerBeat = 24 // This is the MIDI beat clock standard
	private val executorService = Executors.newScheduledThreadPool(1)
	private var playbackHandler: PlaybackHandler? = null
	private class PlaybackHandler : Runnable {
		var stopped = false
		override fun run() {
			var superCount = 0 // Stupid Android logging
			var count = 0
			while (!stopped) {
				val start = System.currentTimeMillis()
				val tickTime: Long = 60000L / (bpm * subdivisionsPerBeat)
				if (count++ == subdivisionsPerBeat) {
					count = 0
					if(superCount == Int.MAX_VALUE) superCount = 0
					else superCount++
					Log.i(BeatClockProducer::class.simpleName, "Quarter $superCount")
				}
				//debug("Tick")
				BeatClockPaletteConsumer.tick()
				Thread.sleep(Math.max(0, tickTime - (System.currentTimeMillis() - start)))
			}
			BeatClockPaletteConsumer.clearActiveAttacks()
		}
	}

	fun startProducing() {
		stopProducing()
		playbackHandler = PlaybackHandler()
		executorService.execute(Thread(playbackHandler))
	}

	fun stopProducing() {
		playbackHandler?.let {
			it.stopped = true
		}
	}
}