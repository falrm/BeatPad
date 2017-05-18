package com.jonlatane.beatpad.instrument

import com.jonlatane.beatpad.sensors.Orientation
import android.util.Log

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
            for (i in 0..numSimultaneousTones - 1) {
                val tone = tones!![toneIndex + i * toneSpread / numSimultaneousTones]
                instrument.play(tone)
            }
        }
    }

    fun stop() {
        instrument.stop()
    }
}
