package com.jonlatane.beatpad.view.palette

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
		relativeLayout {
			if (configuration.portrait) {
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
					elevation=5f
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
			} else {
				val leftSideWidth = dip(350f)

				viewModel.toolbarView = paletteToolbar(viewModel = viewModel) {
					id = R.id.toolbar
				}.lparams {
					width = leftSideWidth
					height = wrapContent
					alignParentLeft()
					alignParentTop()

				}

				viewModel.chordListView = chordListView(viewModel = viewModel) {
					id = R.id.chord_list
				}.lparams {
					width = leftSideWidth
					height = wrapContent
					alignParentLeft()
					alignParentBottom()
				}

				viewModel.orbifold = orbifoldView {
					id = R.id.orbifold
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
				id = R.id.part_list
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

			viewModel.melodyView = melodyView(viewModel = viewModel) {
				id = R.id.melody
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
