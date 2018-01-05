package com.jonlatane.beatpad.view.palette

import android.view.View
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.view.HideableRecyclerView
import com.jonlatane.beatpad.view.melody.PatternViewModel
import kotlin.properties.Delegates.observable

/**
 * The PaletteViewModel still assumes we'll only be editing
 * one Melody at a time.
 */
class PaletteViewModel : PatternViewModel() {
	val palette = Palette()
	var editingSequence by observable<Melody?>(null) { _, _, new ->
		if(new != null) {
			toneSequence = new
			editPatternMode()
		} else patternListMode()
	}
	lateinit var chordListView: View
	lateinit var partListView: HideableRecyclerView
	lateinit var toolbarView: View
	lateinit var keyboardView: View

	fun onBackPressed(): Boolean {
		val result = editingSequence != null
		editingSequence = null
		return result
	}

	private fun editPatternMode() {
		toneSequenceView.animate()
			.translationX(0f)
			.start()
		partListView.animate().alpha(0f)
	}

	private fun patternListMode() {
		toneSequenceView.animate()
			.translationX(toneSequenceView.width.toFloat())
			.start()
		partListView.animate().alpha(1f)
	}
}