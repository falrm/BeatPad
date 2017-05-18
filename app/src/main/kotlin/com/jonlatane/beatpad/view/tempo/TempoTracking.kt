package com.jonlatane.beatpad.view.tempo

import android.view.MotionEvent
import android.view.View

import java.util.Arrays

/**
 * Created by jonlatane on 5/10/17.
 */

object TempoTracking {
    interface TempoChangeListener {
        fun onTempoChanged(tempo: Float)
    }

    fun trackTempo(v: View, onTempoChanged: TempoChangeListener) {
        trackTempo(v, 2, onTempoChanged)
    }

    fun trackTempo(
            v: View,
            sampleWindowSize: Int,
            onTempoChanged: TempoChangeListener
    ) {
        v.setOnTouchListener(object : View.OnTouchListener {
            internal var samples = LongArray(sampleWindowSize)
            internal var samplesTaken = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.getAction() === MotionEvent.ACTION_DOWN) {
                    samples[samplesTaken % sampleWindowSize] = System.currentTimeMillis()
                    if (++samplesTaken >= sampleWindowSize) {
                        reportTempo()
                        samplesTaken = samplesTaken % sampleWindowSize + sampleWindowSize
                    }
                }
                return true
            }

            private fun reportTempo() {
                val sortedSamples = Arrays.copyOfRange(samples, 0, samples.size)
                Arrays.sort(sortedSamples)
                val partialBpms = FloatArray(sortedSamples.size - 1)
                for (i in partialBpms.indices) {
                    partialBpms[i] = 60000f / (sortedSamples[i + 1] - sortedSamples[i]).toFloat()
                }
                var avgBpm = 0f
                for (partialBpm in partialBpms) {
                    avgBpm += partialBpm
                }
                avgBpm /= partialBpms.size.toFloat()

                onTempoChanged.onTempoChanged(avgBpm)
            }
        })
    }
}
