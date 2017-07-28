package com.jonlatane.beatpad.view.tonesequence

import android.view.View
import com.jonlatane.beatpad.harmony.*
import com.jonlatane.beatpad.storage.ToneSequenceStorage
import com.jonlatane.beatpad.view.NonDelayedHorizontalScrollView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.topology.TopologyView
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates
import kotlin.properties.Delegates.observable

class ToneSequenceViewModel {
	var toneSequence: ToneSequence by observable(
		initialValue = ToneSequenceStorage.defaultSequence,
		onChange = { _, _, _ -> redraw() }
	)
	val playing = AtomicBoolean(false)
	lateinit var topology: TopologyView
	var verticalAxis: ToneSequenceAxis? = null
	var chord get() = topology.chord
		set(value) { topology.chord = value; redraw() }
	lateinit var leftScroller: NonDelayedScrollView
	lateinit var bottomScroller: BottomScroller
	lateinit var centerVerticalScroller: NonDelayedScrollView
	lateinit var centerHorizontalScroller: NonDelayedHorizontalScrollView
	lateinit var sequencerThread: ToneSequencePlayerThread
	val elements = mutableListOf<ToneSequenceElement>()
	val bottoms  = mutableListOf<View>()

	internal fun redraw() {
		elements.forEach { it.invalidate() }
		verticalAxis?.invalidate()
	}

	internal fun markPlaying(step: ToneSequence.Step) = markPlaying(
		elements.indexOfFirst { it.step === step }
	)
	internal fun markPlaying(index: Int) {
		elements.forEach {
			if(it.backgroundAlpha == 255) {
				it.backgroundAlpha = 166
				it.invalidate()
			}
		}
		elements[index].apply {
			backgroundAlpha = 255
			invalidate()
		}
	}
}