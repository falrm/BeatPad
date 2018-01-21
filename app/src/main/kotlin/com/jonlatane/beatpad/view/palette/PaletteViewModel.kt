package com.jonlatane.beatpad.view.palette

import android.view.View
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.view.HideableRecyclerView
import com.jonlatane.beatpad.view.colorboard.ColorboardInputView
import com.jonlatane.beatpad.view.keyboard.KeyboardView
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import kotlin.properties.Delegates.observable

/**
 * The PaletteViewModel still assumes we'll only be editing
 * one Melody at a time.
 */
class PaletteViewModel(
) : MelodyViewModel() {
	var palette by observable(Palette()) { _, _, _ ->
		editingSequence = null
	}
	var editingSequence by observable<Melody?>(null) { _, _, new ->
		if(new != null) {
			toneSequence = new
			editPatternMode()
		} else patternListMode()
	}
	lateinit var chordListView: View
	lateinit var partListView: HideableRecyclerView
	lateinit var toolbarView: View
	lateinit var keyboardView: KeyboardView
	lateinit var colorboardView: ColorboardInputView
	var keyboardPart by observable<Part?>(null) { _, _, new ->
		if(new != null) keyboardView.ioHandler.instrument = new.instrument
	}
	var colorboardPart: Part? by observable<Part?>(null) { _, _, new ->
		if(new != null) colorboardView.instrument = new.instrument
	}

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