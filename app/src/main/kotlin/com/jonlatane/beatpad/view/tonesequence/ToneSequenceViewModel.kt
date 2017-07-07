package com.jonlatane.beatpad.view.tonesequence

import android.view.View
import com.jonlatane.beatpad.harmony.ToneSequence
import com.jonlatane.beatpad.harmony.ToneSequence.Step.*
import com.jonlatane.beatpad.util.times
import com.jonlatane.beatpad.view.NonDelayedHorizontalScrollView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.topology.TopologyView
import java.util.concurrent.atomic.AtomicBoolean

class ToneSequenceViewModel(
	val toneSequence: ToneSequence = ToneSequence(
		listOf(
			Note(setOf(0, 4, 7)),
			Note(setOf(7, 12, 16))
		) * 8,
		stepsPerBeat = 4
	)
) {
	val playing = AtomicBoolean(false)
	lateinit var topology: TopologyView
	lateinit var leftScroller: NonDelayedScrollView
	lateinit var bottomScroller: BottomScroller
	lateinit var centerVerticalScroller: NonDelayedScrollView
	lateinit var centerHorizontalScroller: NonDelayedHorizontalScrollView
	val elements = mutableListOf<ToneSequenceElement>()
	val bottoms  = mutableListOf<View>()

	val chord get() = topology.chord

	fun syncViewVisibility() {

	}
}