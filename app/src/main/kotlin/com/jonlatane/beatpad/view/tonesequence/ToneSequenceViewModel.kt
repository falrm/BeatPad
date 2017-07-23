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
		onChange = { _, _, _ -> redraw }
	)
	val playing = AtomicBoolean(false)
	lateinit var topology: TopologyView
	var chord get() = topology.chord
		set(value) { topology.chord = value; redraw }
	lateinit var leftScroller: NonDelayedScrollView
	lateinit var bottomScroller: BottomScroller
	lateinit var centerVerticalScroller: NonDelayedScrollView
	lateinit var centerHorizontalScroller: NonDelayedHorizontalScrollView
	lateinit var sequencerThread: ToneSequencePlayerThread
	val elements = mutableListOf<ToneSequenceElement>()
	val bottoms  = mutableListOf<View>()

	private val redraw get() = elements.forEach { it.invalidate() }
}