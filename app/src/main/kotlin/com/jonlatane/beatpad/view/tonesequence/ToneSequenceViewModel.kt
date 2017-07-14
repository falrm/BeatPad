package com.jonlatane.beatpad.view.tonesequence

import android.view.View
import com.jonlatane.beatpad.harmony.*
import com.jonlatane.beatpad.view.NonDelayedHorizontalScrollView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.topology.TopologyView
import java.util.concurrent.atomic.AtomicBoolean

class ToneSequenceViewModel(
	val toneSequence: ToneSequence = ToneSequence(
		listOf(
			Note(mutableSetOf(0, 4, 7)),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest(),
			Rest()
		),
		stepsPerBeat = 4
	)
) {
	val playing = AtomicBoolean(false)
	lateinit var topology: TopologyView
	var chord get() = topology.chord
		set(value) { topology.chord = value }
	lateinit var leftScroller: NonDelayedScrollView
	lateinit var bottomScroller: BottomScroller
	lateinit var centerVerticalScroller: NonDelayedScrollView
	lateinit var centerHorizontalScroller: NonDelayedHorizontalScrollView
	val elements = mutableListOf<ToneSequenceElement>()
	val bottoms  = mutableListOf<View>()

	fun syncViewVisibility() {

	}
}