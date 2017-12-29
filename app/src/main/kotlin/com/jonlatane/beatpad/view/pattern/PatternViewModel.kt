package com.jonlatane.beatpad.view.pattern

import android.view.View
import com.jonlatane.beatpad.model.Pattern
import com.jonlatane.beatpad.output.controller.ToneSequencePlayerThread
import com.jonlatane.beatpad.storage.PatternStorage
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.NonDelayedRecyclerView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.orbifold.OrbifoldView
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates.observable

open class PatternViewModel {
	var toneSequence by observable<Pattern>(PatternStorage.defaultSequence) { _, _, _ ->
		patternElementAdapter?.notifyDataSetChanged()
	}
	var playbackPosition by observable<Int?>(null) { _, old, new ->
		if(old != null) patternElementAdapter?.notifyItemChanged(old)
		if(new != null) patternElementAdapter?.notifyItemChanged(new)
	}
	val playing = AtomicBoolean(false)
	lateinit var orbifold: OrbifoldView
	var verticalAxis: PatternToneAxis? = null
	var chord
		get() = orbifold.chord
		set(value) {
			orbifold.chord = value; redraw()
		}
	lateinit var toneSequenceView: HideableRelativeLayout
	lateinit var leftScroller: NonDelayedScrollView
	lateinit var bottomScroller: BottomScroller
	lateinit var centerVerticalScroller: NonDelayedScrollView
	lateinit var centerHorizontalScroller: NonDelayedRecyclerView
	var patternElementAdapter: PatternElementAdapter? = null
	lateinit var sequencerThread: ToneSequencePlayerThread
	val bottoms = mutableListOf<View>()

	internal fun redraw() {
		patternElementAdapter?.notifyDataSetChanged()
		verticalAxis?.invalidate()
	}

	internal fun markPlaying(element: Pattern.Element) {
		try {
			playbackPosition = toneSequence.elements.indexOf(element)
		} catch (t: Throwable) {
		}
	}
}