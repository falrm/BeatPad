package com.jonlatane.beatpad.output.controller

import com.jonlatane.beatpad.model.Instrument
import com.jonlatane.beatpad.sensors.Orientation

open class DeviceOrientationInstrument(val instrument: Instrument) {
	var toneSpread = 1.5f
	var numSimultaneousTones = 5
	var tones: List<Int> = emptyList()

	fun play() {
		// Normalize device's physical pitch to a number between 0 and 1
		val relativePitch = Orientation.normalizedDevicePitch()
		//Log.i(TAG, String.format("Relative pitch: %.2f", relativePitch));
		val highestPossibleBottomToneIndex: Float = tones.size - (toneSpread * numSimultaneousTones)
		val toneIndex = (highestPossibleBottomToneIndex * relativePitch).toInt()
		(0..numSimultaneousTones - 1)
			.map { tones[(toneIndex + it * toneSpread).toInt()] }
			.forEach { instrument.play(it) }
	}

	fun stop() {
		instrument.stop()
	}
}
