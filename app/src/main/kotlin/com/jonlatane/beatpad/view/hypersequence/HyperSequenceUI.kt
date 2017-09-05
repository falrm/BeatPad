package com.jonlatane.beatpad.view.hypersequence

import android.app.AlertDialog
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.SequenceEditorActivity
import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.view.keyboard.keyboardView
import com.jonlatane.beatpad.view.nonDelayedHorizontalScrollView
import com.jonlatane.beatpad.view.nonDelayedScrollView
import com.jonlatane.beatpad.view.orbifold.orbifoldView
import com.jonlatane.beatpad.view.tonesequence.*
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onScrollChange
import java.util.concurrent.Executors

class HyperSequenceUI : AnkoComponent<SequenceEditorActivity> {
	companion object {
		private val STEPS_TO_ALLOCATE = 16
	}

	private val executorService = Executors.newScheduledThreadPool(2)
	val viewModel = HyperSequenceViewModel()
	val previewInstrument = MIDIInstrument().apply {
		channel = 4
		instrument = GeneralMidiConstants.SYNTH_BASS_1
	}
	val sequencerInstrument = MIDIInstrument().apply {
		channel = 5
		instrument = GeneralMidiConstants.SYNTH_BASS_1
	}


	override fun createView(ui: AnkoContext<SequenceEditorActivity>) = with(ui) {
		var IDSeq = 1 // Literally just a source of View IDs to make Android happy.
		relativeLayout {
			viewModel.toneSequence.orbifold = orbifoldView {
				id = IDSeq++
				onChordChangedListener = {
					viewModel.toneSequence.verticalAxis?.chord = it
					viewModel.toneSequence.redraw()
				}
			}.lparams {
				if (configuration.portrait) {
					width = MATCH_PARENT
					height = dip(210f)
					alignParentTop()
				} else {
					width = dip(350f)
					height = MATCH_PARENT
					alignParentLeft()
				}
			}
			toneSequenceView(viewModel = viewModel.toneSequence, ui = ui) {

			}.lparams {

			}

			keyboardView {

			}.lparams {

			}
		}
	}
}
