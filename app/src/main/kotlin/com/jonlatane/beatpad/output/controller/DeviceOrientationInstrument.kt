package com.jonlatane.beatpad.output.controller

import com.jonlatane.beatpad.output.instrument.Instrument
import com.jonlatane.beatpad.sensors.Orientation

/**
 * Created by jonlatane on 5/8/17.
 */

open class DeviceOrientationInstrument(val instrument: Instrument) {
    var toneSpread = 5
    var numSimultaneousTones = 5
    private var tones: List<Int>? = null
    fun setTones(tones: List<Int>) {
        this.tones = tones
    }

    fun play() {
        // Play the notes
        if (tones != null) {
            // Normalize device's physical pitch to a number between 0 and 1
            val relativePitch = Orientation.normalizedDevicePitch()
            //Log.i(TAG, String.format("Relative pitch: %.2f", relativePitch));
            val toneIndex = Math.round((tones!!.size - toneSpread) * relativePitch).toInt()
            (0..numSimultaneousTones - 1)
              .map { tones!![toneIndex + it * toneSpread / numSimultaneousTones] }
              .forEach { instrument.play(it) }
        }
    }

    fun stop() {
        instrument.stop()
    }
}
