package com.jonlatane.beatpad.output.service

import android.util.Log
import com.jonlatane.beatpad.output.service.BeatClockProducerThread.bpm
import com.jonlatane.beatpad.output.service.BeatClockProducerThread.stopped
import com.jonlatane.beatpad.output.service.BeatClockProducerThread.subdivisionsPerBeat
import org.jetbrains.anko.AnkoLogger
import java.util.concurrent.Executors

object BeatClockProducerThread : AnkoLogger, Thread(Runnable {
	while (!stopped) {
		val tickTime = 60000L / (bpm * subdivisionsPerBeat)
		Log.i(BeatClockProducerThread::class.simpleName, "Tick")
		//debug("Tick")
		//BeatClockPaletteConsumer.tick()
		Thread.sleep(tickTime)
	}
}) {
	private val executorService = Executors.newScheduledThreadPool(1)
	@Volatile
	var stopped = true
	private set
	var bpm: Int = 120
	const val subdivisionsPerBeat = 24 // This is the MIDI beat clock standard

	fun startProducing() {
		BeatClockProducerThread.stopped = false
		executorService.execute(BeatClockProducerThread)
	}

	fun stopProducing() {
		BeatClockProducerThread.stopped = true
	}
}