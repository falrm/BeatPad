package com.jonlatane.beatpad.view.palette

import android.view.View
import com.jonlatane.beatpad.harmony.chord.*
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.view.tonesequence.ToneSequenceViewModel

/**
 * The PaletteViewModel still assumes we'll only be editing
 * one ToneSequence at a time.
 */
class PaletteViewModel : ToneSequenceViewModel() {
	val palette = Palette()
	lateinit var chordListView: View
	lateinit var partListView: View
	lateinit var toolbarView: View
	lateinit var keyboardView: View
}