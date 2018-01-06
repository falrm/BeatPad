package com.jonlatane.beatpad.view.harmony

import android.view.View
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.output.controller.ToneSequencePlayerThread
import com.jonlatane.beatpad.storage.MelodyStorage
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.NonDelayedRecyclerView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.melody.BottomScroller
import com.jonlatane.beatpad.view.melody.PatternElementAdapter
import com.jonlatane.beatpad.view.melody.PatternToneAxis
import com.jonlatane.beatpad.view.orbifold.OrbifoldView
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates.observable

open class HarmonyViewModel {
	var harmony by observable(Harmony()) { _, _, _ ->
		chordAdapter?.notifyDataSetChanged()
	}
	var playbackPosition: Int? by observable<Int?>(null) { _, old, new ->
		if(old != null) chordAdapter?.notifyItemChanged(old)
		if(new != null) chordAdapter?.notifyItemChanged(new)
	}
	val playing = AtomicBoolean(false)
	lateinit var orbifold: OrbifoldView
	var verticalAxis: PatternToneAxis? = null
	var chord
		get() = orbifold.chord
		set(value) {
			orbifold.chord = value; redraw()
		}
	lateinit var harmonyView: HideableRelativeLayout
	lateinit var centerHorizontalScroller: NonDelayedRecyclerView
	var chordAdapter: PatternElementAdapter? = null
	lateinit var sequencerThread: ToneSequencePlayerThread
	val bottoms = mutableListOf<View>()

	internal fun redraw() {
		chordAdapter?.notifyDataSetChanged()
		verticalAxis?.invalidate()
	}

	internal fun markPlaying(element: Melody.Element) {
		/*try {
			playbackPosition = harmony.elements.indexOf(element)
		} catch (t: Throwable) {
		}*/
	}
}