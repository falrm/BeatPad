package com.jonlatane.beatpad.view.melody

import android.view.View
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.output.controller.ToneSequencePlayerThread
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.NonDelayedRecyclerView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.orbifold.OrbifoldView
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates.observable

open class MelodyViewModel {
	var toneSequence by observable<Melody>(PaletteStorage.baseMelody) { _, _, _ ->
		melodyElementAdapter?.notifyDataSetChanged()
	}
	var playbackPosition by observable<Int?>(null) { _, old, new ->
		if(old != null) melodyElementAdapter?.notifyItemChanged(old)
		if(new != null) melodyElementAdapter?.notifyItemChanged(new)
	}
	val playing = AtomicBoolean(false)
	lateinit var orbifold: OrbifoldView
	var verticalAxis: MelodyToneAxis? = null
	var chord
		get() = orbifold.chord
		set(value) {
			orbifold.chord = value; redraw()
		}
	lateinit var melodyView: HideableRelativeLayout
	lateinit var melodyLeftScroller: NonDelayedScrollView
	lateinit var melodyBottomScroller: BottomScroller
	lateinit var melodyCenterVerticalScroller: NonDelayedScrollView
	lateinit var melodyCenterHorizontalScroller: NonDelayedRecyclerView
	var melodyElementAdapter: MelodyElementAdapter? = null
	lateinit var sequencerThread: ToneSequencePlayerThread
	val bottoms = mutableListOf<View>()

	internal fun redraw() {
		melodyElementAdapter?.notifyDataSetChanged()
		verticalAxis?.invalidate()
	}

	internal fun markPlaying(element: Melody.Element) {
		try {
			playbackPosition = toneSequence.elements.indexOf(element)
		} catch (t: Throwable) {
		}
	}
}