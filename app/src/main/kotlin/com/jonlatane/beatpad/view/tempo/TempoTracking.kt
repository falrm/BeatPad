package com.jonlatane.beatpad.view.tempo

import android.view.MotionEvent
import android.view.View
import org.jetbrains.anko.sdk25.coroutines.onClick

import java.util.Arrays

/**
 * Created by jonlatane on 5/10/17.
 */

object TempoTracking {

	fun trackTempo(v: View, onTempoChanged: (Float) -> Unit) {
		trackTempo(v, 2, onTempoChanged)
	}

	fun trackTempo(
		v: View,
		sampleWindowSize: Int,
		onTempoChanged: (Float) -> Unit
	) {
		v.setOnClickListener(object : View.OnClickListener {

			internal var samples = LongArray(sampleWindowSize)
			internal var samplesTaken = 0
			override fun onClick(v: View?) {
				samples[samplesTaken % sampleWindowSize] = System.currentTimeMillis()
				if (++samplesTaken >= sampleWindowSize) {
					reportTempo()
					samplesTaken = samplesTaken % sampleWindowSize + sampleWindowSize
				}
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

				onTempoChanged(avgBpm)
			}
		})
	}
}
