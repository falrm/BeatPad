package com.jonlatane.beatpad.output.service

import com.jonlatane.beatpad.output.service.BeatClockProducerThread.bpm
import com.jonlatane.beatpad.output.service.BeatClockProducerThread.stopped
import com.jonlatane.beatpad.output.service.BeatClockProducerThread.subdivisionsPerBeat
import org.jetbrains.anko.AnkoLogger
import java.util.concurrent.Executors

object BeatClockProducerThread : Thread(Runnable {
	while (!stopped) {
		val tickTime = 60000L / (bpm * subdivisionsPerBeat)
		//debug("Tick")
		//BeatClockPaletteConsumer.tick()
		Thread.sleep(tickTime)
	}
}), AnkoLogger {
	private val executorService = Executors.newScheduledThreadPool(1)
	@Volatile
	var stopped = true
	var bpm: Int = 120
	const val subdivisionsPerBeat = 24 // This is the MIDI beat clock standard

	fun startProducing() {
		BeatClockProducerThread.stopped = false
		executorService.execute(BeatClockProducerThread)
	}

	fun stopProducting() {
		BeatClockProducerThread.stopped = true
	}
}