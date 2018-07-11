package com.jonlatane.beatpad.view.palette

import android.support.constraint.ConstraintSet
import com.jonlatane.beatpad.PaletteEditorActivity
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.view.colorboard.colorboardView
import com.jonlatane.beatpad.view.keyboard.keyboardView
import com.jonlatane.beatpad.view.melody.melodyView
import com.jonlatane.beatpad.view.orbifold.orbifoldView
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.*
import java.util.concurrent.Executors
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import android.support.constraint.ConstraintSet.PARENT_ID
import org.jetbrains.anko.constraint.layout.*

class PaletteUI : AnkoComponent<PaletteEditorActivity> {
	private val executorService = Executors.newScheduledThreadPool(2)
	val viewModel = PaletteViewModel()
	val useConstraintLayout = false
	val previewInstrument = MIDIInstrument().apply {
		channel = 4
		instrument = GeneralMidiConstants.SYNTH_BASS_1
	}
	val sequencerInstrument = MIDIInstrument().apply {
		channel = 5
		instrument = GeneralMidiConstants.SYNTH_BASS_1
	}


	override fun createView(ui: AnkoContext<PaletteEditorActivity>) = with(ui) {
		if(useConstraintLayout) {
			constraintLayout {
				constraintLayoutViews()
				if (configuration.portrait) {
					applyConstraintSet {
						portraitConstraints(this@constraintLayout)
					}
				} else {
					applyConstraintSet {
						portraitConstraints(this@constraintLayout)
					}
				}

				viewModel.orbifold.onChordChangedListener = { c: Chord ->
					val tones = c.getTones()
					viewModel.colorboardView.chord = c
					//viewModel.harmonyController.tones = tones
					viewModel.keyboardView.ioHandler.highlightChord(c)
					viewModel.verticalAxis?.chord = c
					viewModel.splatController?.tones = c.getTones()
					viewModel.redraw()
				}

				post {
					viewModel.partListView.animate()
						.alpha(1f)
						.start()
					viewModel.melodyView.animate()
						.translationX(viewModel.melodyView.width.toFloat())
						.withEndAction { viewModel.melodyView.alpha = 1f }
						.start()
					/*listOf<View>(viewModel.keyboardView, viewModel.colorboardView).forEach {
					it.hide(false)
					it.alpha = 1f
				}*/
				}
			}
		} else {
			relativeLayout {
				if (configuration.portrait) {
					portraitLayout()
				} else {
					landscapeLayout()
				}

				keyboardsLayout()


				viewModel.orbifold.onChordChangedListener = { c: Chord ->
					val tones = c.getTones()
					viewModel.colorboardView.chord = c
					//viewModel.harmonyController.tones = tones
					viewModel.keyboardView.ioHandler.highlightChord(c)
					viewModel.verticalAxis?.chord = c
					viewModel.splatController?.tones = c.getTones()
					viewModel.redraw()
				}

				post {
					viewModel.partListView.animate()
						.alpha(1f)
						.start()
					viewModel.melodyView.animate()
						.translationX(viewModel.melodyView.width.toFloat())
						.withEndAction { viewModel.melodyView.alpha = 1f }
						.start()
					/*listOf<View>(viewModel.keyboardView, viewModel.colorboardView).forEach {
					it.hide(false)
					it.alpha = 1f
				}*/
				}
			}
		}
	}

	private fun _ConstraintLayout.constraintLayoutViews() {
		viewModel.orbifold = orbifoldView {
			id = R.id.orbifold
		}

		viewModel.chordListView = chordListView(viewModel = viewModel) {
			id = R.id.chord_list
		}

		viewModel.toolbarView = paletteToolbar(viewModel = viewModel) {
			id = R.id.toolbar
		}

		viewModel.partListView = partListView(viewModel = viewModel) {
			id = R.id.part_list
		}

		viewModel.melodyView = melodyView(viewModel = viewModel) {
			id = R.id.melody
			alpha = 0f
		}

		viewModel.keyboardView = keyboardView {
			id = R.id.keyboard
			elevation = 10f
			//alpha = 0f
		}

		viewModel.colorboardView = colorboardView {
			id = R.id.colorboard
			elevation = 10f
			//alpha = 0f
			backgroundColor = color(android.R.color.white)
			//translationY = dimen(R.dimen.key_height_white).toFloat()
		}
		viewModel.apply {
			listOf(orbifold,
				chordListView,
				toolbarView, partListView, melodyView, keyboardView,
				colorboardView). apply {
				lparams(matchConstraint, matchConstraint)
			}
		}
	}

	private fun ConstraintSetBuilder.portraitConstraints(layout: _ConstraintLayout) = with(layout) {
		viewModel.orbifold {
			connect(
				TOP to TOP of PARENT_ID,
				LEFT to LEFT of PARENT_ID,
				RIGHT to RIGHT of PARENT_ID
			)
			height = dip(200)
			horizontalBias = 1.0f
			verticalBias = 0.0f
		}

		viewModel.chordListView {
			connect(
				TOP to BOTTOM of viewModel.orbifold,
				START to START of PARENT_ID,
				END to END of PARENT_ID
			)
			height = wrapContent
			elevation = 5f
		}

		viewModel.toolbarView {
			connect(
				TOP to BOTTOM of viewModel.chordListView,
				START to START of PARENT_ID,
				END to END of PARENT_ID
			)
			height = wrapContent
		}

		viewModel.partListView {
			connect(
				TOP to BOTTOM of viewModel.toolbarView,
				START to START of PARENT_ID,
				END to END of PARENT_ID,
				BOTTOM to BOTTOM of PARENT_ID
			)
			verticalBias = 0.0f
		}

		viewModel.melodyView {
			connect(
				TOP to TOP of viewModel.partListView,
				START to START of viewModel.partListView,
				END to END of viewModel.partListView,
				BOTTOM to BOTTOM of viewModel.partListView
			)
		}

		viewModel.keyboardView {
			connect(
				START to START of viewModel.partListView,
				END to END of viewModel.partListView,
				BOTTOM to BOTTOM of PARENT_ID
			)

			height = dimen(R.dimen.key_height_white)
			alpha = 0.5f
		}

		viewModel.colorboardView {
			connect(
				START to START of viewModel.partListView,
				END to END of viewModel.partListView,
				BOTTOM to TOP of viewModel.keyboardView
			)
			height = dimen(R.dimen.key_height_white)
			alpha = 0.5f
		}
	}

	private fun _RelativeLayout.portraitLayout() {
		viewModel.orbifold = orbifoldView {
			id = R.id.orbifold
		}.lparams {
			width = matchParent
			height = dip(210f)
			alignParentTop()
		}

		viewModel.chordListView = chordListView(viewModel = viewModel) {
			id = R.id.chord_list
		}.lparams {
			below(viewModel.orbifold)
			elevation = 5f
			width = matchParent
			height = wrapContent
		}

		viewModel.toolbarView = paletteToolbar(viewModel = viewModel) {
			id = R.id.toolbar
		}.lparams {
			below(viewModel.chordListView)
			width = matchParent
			height = wrapContent
		}

		viewModel.partListView = partListView(viewModel = viewModel) {
			id = R.id.part_list
		}.lparams {
			width = matchParent
			height = wrapContent
			alignParentBottom()
			below(viewModel.toolbarView)
		}

		viewModel.melodyView = melodyView(viewModel = viewModel) {
			id = R.id.melody
			alpha = 0f
		}.lparams {
			width = matchParent
			height = wrapContent
			alignParentBottom()
			below(viewModel.toolbarView)
		}
	}

	private fun _RelativeLayout.landscapeLayout() {
		val leftSideWidth = dip(350f)

		viewModel.chordListView = chordListView(viewModel = viewModel) {
			id = R.id.chord_list
		}.lparams {
			width = leftSideWidth
			height = wrapContent
			alignParentLeft()
			alignParentTop()
		}

		viewModel.orbifold = orbifoldView {
			id = R.id.orbifold
		}.lparams {
			alignParentLeft()
			below(viewModel.chordListView)
			width = leftSideWidth
			height = matchParent
			elevation = 5f
		}

		viewModel.toolbarView = paletteToolbar(viewModel = viewModel) {
			id = R.id.toolbar
		}.lparams {
			width = matchParent
			height = wrapContent
			rightOf(viewModel.orbifold)
			alignParentTop()
			alignParentRight()

		}

		viewModel.partListView = partListView(viewModel = viewModel) {
			id = R.id.part_list
		}.lparams {
			width = matchParent
			height = wrapContent
			alignParentBottom()
			rightOf(viewModel.orbifold)
			below(viewModel.toolbarView)
			alignParentRight()
		}

		viewModel.melodyView = melodyView(viewModel = viewModel) {
			id = R.id.melody
			alpha = 0f
		}.lparams {
			width = matchParent
			height = wrapContent
			alignParentBottom()
			rightOf(viewModel.orbifold)
			below(viewModel.toolbarView)
			alignParentRight()
		}
	}

	private fun _RelativeLayout.keyboardsLayout() {

		viewModel.keyboardView = keyboardView {
			id = R.id.keyboard
			elevation = 10f
			//alpha = 0f
			//translationY = dimen(R.dimen.key_height_white).toFloat()
		}.lparams {
			height = dimen(R.dimen.key_height_white)
			width = matchParent
			alignParentBottom()
		}

		viewModel.colorboardView = colorboardView {
			id = R.id.colorboard
			elevation = 10f
			//alpha = 0f
			backgroundColor = color(android.R.color.white)
			//translationY = dimen(R.dimen.key_height_white).toFloat()
		}.lparams {
			height = dimen(R.dimen.key_height_white)
			width = matchParent
			above(viewModel.keyboardView)
		}
	}
}
