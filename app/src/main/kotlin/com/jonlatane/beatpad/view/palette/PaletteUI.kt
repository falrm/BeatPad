package com.jonlatane.beatpad.view.palette

import android.app.AlertDialog
import android.view.Gravity
import com.jonlatane.beatpad.PaletteEditorActivity
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Rest
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.view.keyboard.keyboardView
import com.jonlatane.beatpad.view.orbifold.orbifoldView
import com.jonlatane.beatpad.view.tonesequence.toneSequenceView
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
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
					onChordChangedListener = {
						viewModel.verticalAxis?.chord = it
						viewModel.redraw()
					}
				}.lparams {
					width = matchParent
					height = dip(210f)
					alignParentTop()
				}

				viewModel.chordListView = chordListView(viewModel = viewModel) {
					id = IDSeq++
				}.lparams {
					below(viewModel.orbifold)
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

			toneSequenceView(viewModel = viewModel) {
				id = IDSeq++
			}.lparams {
				width = matchParent
				height = matchParent
				below(viewModel.partListView)
				alignParentBottom()
				alignParentRight()
				if(configuration.landscape) {
					rightOf(viewModel.orbifold)
				}
			}

			viewModel.keyboardView = keyboardView {

			}.lparams {
				height = dimen(R.dimen.key_height_white)
				width = matchParent
				elevation = 10f
				alignParentBottom()
			}
		}
	}
}
