package com.jonlatane.beatpad.output.service

import BeatClockPaletteConsumer
import BeatClockPaletteConsumer.tickPosition
import android.util.Log
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import java.util.concurrent.Executors

object BeatClockProducer : AnkoLogger {
	var bpm: Int = 120
	private const val subdivisionsPerBeat = 24 // This is the MIDI beat clock standard
	private val executorService = Executors.newScheduledThreadPool(1)
	private object PlaybackHandler : Runnable {
		var stopped = false
		override fun run() {
			while (!stopped) {
				val start = System.currentTimeMillis()
				val tickTime: Long = 60000L / (bpm * subdivisionsPerBeat)
				when {
					tickPosition % subdivisionsPerBeat == 0 -> { tickPosition / subdivisionsPerBeat }
					else -> null
				}?.let {
					info("Quarter #$it")
				}
				info("Tick @${BeatClockPaletteConsumer.tickPosition}")
				BeatClockPaletteConsumer.tick()
				val sleepTime = (tickTime - (System.currentTimeMillis() - start)).let {
					when {
						it < 0 -> 0L
						it > 800 -> 800L
						else -> it
					}
				}
				Thread.sleep(sleepTime)
			}
			BeatClockPaletteConsumer.clearActiveAttacks()
		}
	}

	fun startProducing() {
		stopProducing()
		PlaybackHandler.stopped = false
		executorService.execute(Thread(PlaybackHandler))
	}

	fun stopProducing() {
		PlaybackHandler.stopped = true
	}
}