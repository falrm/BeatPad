package com.jonlatane.beatpad.view.melody

import android.app.AlertDialog
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.SequenceEditorActivity
import com.jonlatane.beatpad.model.Rest
import com.jonlatane.beatpad.output.controller.ToneSequencePlayerThread
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.view.orbifold.orbifoldView
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.concurrent.Executors

class MelodyUI : AnkoComponent<SequenceEditorActivity> {
	companion object Companion {
		val STEPS_TO_ALLOCATE = 16
	}

	private val executorService = Executors.newScheduledThreadPool(2)
	val viewModel = MelodyViewModel()
	val previewInstrument = MIDIInstrument().apply {
		channel = 4
		instrument = GeneralMidiConstants.SYNTH_BASS_1
	}
	val sequencerInstrument = MIDIInstrument().apply {
		channel = 5
		instrument = GeneralMidiConstants.SYNTH_BASS_1
	}
	val sequencerThread get() = viewModel.sequencerThread


	override fun createView(ui: AnkoContext<SequenceEditorActivity>) = with(ui) {
		viewModel.sequencerThread = ToneSequencePlayerThread(sequencerInstrument, viewModel, beatsPerMinute = 120)
		var holdToEdit: TextView? = null
		relativeLayout {
			viewModel.orbifold = orbifoldView {
				id = R.id.orbifold
				onChordChangedListener = {
					viewModel.verticalAxis?.chord = it
					viewModel.redraw()
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

			melodyView(viewModel = viewModel) {
				id = R.id.melody
			}.lparams {
				width = MATCH_PARENT
				height = MATCH_PARENT
				if (configuration.portrait) {
					below(viewModel.orbifold)
					alignParentBottom()
				} else {
					rightOf(viewModel.orbifold)
					alignParentTop()
					alignParentRight()
					alignParentBottom()
				}
			}

			button {
				text = "Play"
				onClick {
					if (!viewModel.playing.getAndSet(true)) {
						sequencerThread.stopped = false
						executorService.execute(sequencerThread)
						text = "Stop"
					} else {
						sequencerThread.stopped = true
						AudioTrackCache.releaseAll()
						viewModel.playing.set(false)
						text = "Play"
					}
				}
			}.lparams {
				width = WRAP_CONTENT
				height = WRAP_CONTENT
				below(viewModel.melodyCenterVerticalScroller)
				alignParentBottom()
				gravity = Gravity.CENTER_VERTICAL
				if (configuration.portrait) {
					alignParentLeft()
				} else {
					rightOf(viewModel.orbifold)
				}
			}

			button {
				text = "Clear"
				onClick {
					AlertDialog.Builder(this@button.context)
						.setTitle("Clear Sequence Data")
						.setMessage("Really delete all your hard work?")
						.setPositiveButton("Yes, it's just art") { _, _ ->
							for (index in viewModel.openedMelody.elements.indices) {
								viewModel.openedMelody.elements[index] = Rest()
								viewModel.redraw()
							}
						}
						.setNegativeButton("No", null)
						.show()
				}
			}.lparams {
				width = WRAP_CONTENT
				height = WRAP_CONTENT
				alignParentRight()
				below(viewModel.melodyCenterVerticalScroller)
				alignParentBottom()
				gravity = Gravity.CENTER_VERTICAL
			}
		}
	}
}