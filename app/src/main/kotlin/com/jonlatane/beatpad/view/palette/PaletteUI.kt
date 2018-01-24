package com.jonlatane.beatpad.view.palette

import android.view.View
import com.jonlatane.beatpad.PaletteEditorActivity
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.util.hide
import com.jonlatane.beatpad.view.colorboard.colorboardView
import com.jonlatane.beatpad.view.keyboard.keyboardView
import com.jonlatane.beatpad.view.melody.melodyView
import com.jonlatane.beatpad.view.orbifold.orbifoldView
import kotlinx.android.synthetic.main.activity_main.*
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.*
import java.util.concurrent.Executors

class PaletteUI : AnkoComponent<PaletteEditorActivity> {
	private val executorService = Executors.newScheduledThreadPool(2)
	val viewModel = PaletteViewModel()
	val previewInstrument = MIDIInstrument().apply {
		channel = 4
		instrument = GeneralMidiConstants.SYNTH_BASS_1
	}
	val sequencerInstrument = MIDIInstrument().apply {
		channel = 5
		instrument = GeneralMidiConstants.SYNTH_BASS_1
	}
	val sequencerThread get() = viewModel.sequencerThread


	override fun createView(ui: AnkoContext<PaletteEditorActivity>) = with(ui) {
		var IDSeq = 1 // Literally just a source of View IDs to make Android happy.
		relativeLayout {
			if (configuration.portrait) {
				viewModel.orbifold = orbifoldView {
					id = IDSeq++
				}.lparams {
					width = matchParent
					height = dip(210f)
					alignParentTop()
				}

				viewModel.chordListView = chordListView(viewModel = viewModel) {
					id = IDSeq++
				}.lparams {
					below(viewModel.orbifold)
					elevation=5f
					width = matchParent
					height = wrapContent
				}

				viewModel.toolbarView = paletteToolbar(viewModel = viewModel) {
					id = IDSeq++
				}.lparams {
					below(viewModel.chordListView)
					width = matchParent
					height = wrapContent
				}
			} else {
				val leftSideWidth = dip(350f)

				viewModel.toolbarView = paletteToolbar(viewModel = viewModel) {
					id = IDSeq++
				}.lparams {
					width = leftSideWidth
					height = wrapContent
					alignParentLeft()
					alignParentTop()

				}

				viewModel.chordListView = chordListView(viewModel = viewModel) {
					id = IDSeq++
				}.lparams {
					width = leftSideWidth
					height = wrapContent
					alignParentLeft()
					alignParentBottom()
				}

				viewModel.orbifold = orbifoldView {
					id = IDSeq++
					onChordChangedListener = {
						viewModel.verticalAxis?.chord = it
						viewModel.redraw()
					}
				}.lparams {
					alignParentLeft()
					above(viewModel.chordListView)
					below(viewModel.toolbarView)
					width = leftSideWidth
					height = matchParent
					elevation = 5f
				}
			}

			viewModel.partListView = partListView(viewModel = viewModel) {
				id = IDSeq++
			}.lparams {
				width = matchParent
				height = wrapContent
				alignParentBottom()
				if (configuration.portrait) {
					below(viewModel.toolbarView)
				} else {
					rightOf(viewModel.orbifold)
					alignParentTop()
					alignParentRight()
				}
			}

			viewModel.toneSequenceView = melodyView(viewModel = viewModel) {
				id = IDSeq++
				alpha = 0f
			}.lparams {
				width = matchParent
				height = wrapContent
				alignParentBottom()
				if (configuration.portrait) {
					below(viewModel.toolbarView)
				} else {
					rightOf(viewModel.orbifold)
					alignParentTop()
					alignParentRight()
				}
			}

			viewModel.keyboardView = keyboardView {
				id = IDSeq++
				elevation = 10f
				alpha = 0f
				//translationY = dimen(R.dimen.key_height_white).toFloat()
			}.lparams {
				height = dimen(R.dimen.key_height_white)
				width = matchParent
				alignParentBottom()
			}

			viewModel.colorboardView = colorboardView {
				elevation = 10f
				alpha = 0f
				backgroundColor = color(android.R.color.white)
				//translationY = dimen(R.dimen.key_height_white).toFloat()
			}.lparams {
				height = dimen(R.dimen.key_height_white)
				width = matchParent
				above(viewModel.keyboardView)
			}

			post {
				viewModel.partListView.animate()
					.alpha(1f)
					.start()
				viewModel.toneSequenceView.animate()
					.translationX(viewModel.toneSequenceView.width.toFloat())
					.withEndAction { viewModel.toneSequenceView.alpha = 1f }
					.start()
				listOf<View>(viewModel.keyboardView, viewModel.colorboardView).forEach {
					it.hide(false)
					it.alpha = 1f
				}

				viewModel.orbifold.onChordChangedListener = { c: Chord ->
					val tones = c.getTones()
					viewModel.colorboardView.chord = c
					//viewModel.harmonyController.tones = tones
					viewModel.keyboardView.ioHandler.highlightChord(c)
					viewModel.verticalAxis?.chord = c
					viewModel.redraw()
				}
			}
		}
	}
}
