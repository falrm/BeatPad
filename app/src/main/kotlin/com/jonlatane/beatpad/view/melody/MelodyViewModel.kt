package com.jonlatane.beatpad.view.melody

import BeatClockPaletteConsumer
import android.view.View
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.NonDelayedRecyclerView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.orbifold.OrbifoldView
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.floor
import kotlin.properties.Delegates.observable

open class MelodyViewModel {
	var openedMelody by observable<Melody<*>?>(PaletteStorage.baseMelody) { _, _, _ ->
		melodyElementAdapter?.notifyDataSetChanged()
	}

	/**TODO: This should really be based on [BeatClockPaletteConsumer.ticksPerBeat] */
	var playbackPosition by observable<Int?>(null) { _, old, new ->
		old?.let { melodyView.post { melodyElementAdapter?.invalidate(it) } }
		new?.let { melodyView.post { melodyElementAdapter?.invalidate(it) } }
	}
	val playing = AtomicBoolean(false)
	lateinit var orbifold: OrbifoldView
	var verticalAxis: MelodyToneAxis? = null
	var chord
		get() = orbifold.chord
		set(value) {
			orbifold.chord = value; redraw()
		}
	lateinit var melodyToolbar: MelodyToolbar
	lateinit var melodyView: HideableRelativeLayout
	lateinit var melodyLeftScroller: NonDelayedScrollView
	lateinit var melodyEditingModifiers: MelodyEditingModifiers
	lateinit var melodyCenterVerticalScroller: NonDelayedScrollView
	lateinit var melodyCenterHorizontalScroller: NonDelayedRecyclerView
	var melodyElementAdapter: MelodyBeatAdapter? = null
	val bottoms = mutableListOf<View>()

	internal fun redraw() {
		melodyElementAdapter?.notifyDataSetChanged()
		verticalAxis?.invalidate()
	}

	internal fun markPlaying(tickPosition: Int) {
		try {
      openedMelody?.let {
        val currentBeat: Double = tickPosition.toDouble() / BeatClockPaletteConsumer.ticksPerBeat
        val melodyLength: Double = it.length.toDouble() / it.subdivisionsPerBeat
        val positionInMelody: Double = currentBeat % melodyLength

        // This is the closest element in the current melody to our tick position
        val indexCandidate = floor(positionInMelody * it.subdivisionsPerBeat).toInt()

        playbackPosition = indexCandidate
      }
		} catch (t: Throwable) {
		}
	}
}