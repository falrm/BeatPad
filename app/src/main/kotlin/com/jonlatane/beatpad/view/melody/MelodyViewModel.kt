package com.jonlatane.beatpad.view.melody

import android.view.View
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.NonDelayedRecyclerView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates.observable

class MelodyViewModel(
  val paletteViewModel: PaletteViewModel
) {
	var openedMelody by observable<Melody<*>?>(PaletteStorage.baseMelody) { _, _, _ ->
		melodyElementAdapter.notifyDataSetChanged()
    melodyToolbar.updateButtonText()
	}
	val playing = AtomicBoolean(false)
	var verticalAxis: MelodyToneAxis? = null
	lateinit var melodyToolbar: MelodyToolbar
	lateinit var melodyView: HideableRelativeLayout
	lateinit var melodyLeftScroller: NonDelayedScrollView
	lateinit var melodyEditingModifiers: MelodyEditingModifiers
	lateinit var melodyCenterVerticalScroller: NonDelayedScrollView
	lateinit var melodyCenterHorizontalScroller: NonDelayedRecyclerView
	lateinit var melodyElementAdapter: MelodyBeatAdapter

	internal fun redraw() {
		melodyElementAdapter.notifyDataSetChanged()
		verticalAxis?.invalidate()
	}
}