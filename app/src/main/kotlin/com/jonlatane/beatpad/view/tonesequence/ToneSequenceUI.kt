package com.jonlatane.beatpad.view.tonesequence

import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.SequenceEditorActivity
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.view.nonDelayedHorizontalScrollView
import com.jonlatane.beatpad.view.nonDelayedScrollView
import com.jonlatane.beatpad.view.topology.topologyView
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onScrollChange
import java.util.concurrent.Executors

class ToneSequenceUI : AnkoComponent<SequenceEditorActivity> {
	companion object { private val STEPS_TO_ALLOCATE = 16 }
	private val executorService = Executors.newScheduledThreadPool(2)
	val viewModel = ToneSequenceViewModel()
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
		var IDSeq = 1 // Literally just a source of View IDs to make Android happy.
		var holdToEdit: TextView? = null
		relativeLayout {
			viewModel.topology = topologyView {
				id = IDSeq++
				onChordChangedListener = { viewModel.elements.forEach { it.invalidate() } }
			}.lparams {
				if(configuration.portrait) {
					width = MATCH_PARENT
					height = dip(210f)
					alignParentTop()
				} else {
					width = dip(350f)
					height = MATCH_PARENT
					alignParentLeft()
				}
			}
			viewModel.bottomScroller = bottomScroller {
				id = IDSeq++
				onHeldDownChanged = { heldDown ->
					if(heldDown) holdToEdit?.animate()?.alpha(0f)?.translationY(100f)
					else holdToEdit?.animate()?.alpha(1f)?.translationY(0f)
				}
				linearLayout {
					orientation = HORIZONTAL
					repeat(STEPS_TO_ALLOCATE) {
						viewModel.bottoms.add(
							view {
								background = ctx.getDrawable(R.drawable.tone_footer)
							}.lparams {
								width = dimen(R.dimen.subdivision_controller_size)
								height = dimen(R.dimen.subdivision_controller_size)
							}
						)
					}
				}
				scrollingEnabled = false
			}.lparams {
				alignParentBottom()
				alignParentRight()
				width = MATCH_PARENT
				height = dimen(R.dimen.subdivision_controller_size)
				leftMargin = dip(30)
				if(configuration.landscape) {
					rightOf(viewModel.topology)
				}
			}
			holdToEdit = textView {
				text = "Hold To Edit"
				textSize = 10f
				gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
			}.lparams {
				alignParentBottom()
				alignParentRight()
				width = MATCH_PARENT
				height = dimen(R.dimen.subdivision_controller_size)
				leftMargin = dip(30)
				if(configuration.landscape) {
					rightOf(viewModel.topology)
				}
			}
			viewModel.leftScroller = nonDelayedScrollView {
				id = IDSeq++
				linearLayout {
					toneSequenceAxis()
					/*view {
						background = ctx.getDrawable(R.drawable.vertical_keyboard)
					}*/.lparams {
						width = dip(30)
						height = dip(1000f)
					}
				}
				scrollingEnabled = false
				isVerticalScrollBarEnabled = false
			}.lparams {
				width = dip(30)
				height = MATCH_PARENT
				above(viewModel.bottomScroller)
				if(configuration.portrait) {
					alignParentLeft()
					below(viewModel.topology)
				} else {
					rightOf(viewModel.topology)
				}
			}
			viewModel.centerVerticalScroller = nonDelayedScrollView {
				onScrollChange {
					_, _, scrollY, _, _ ->
					viewModel.leftScroller.scrollY = scrollY
				}
				viewModel.centerHorizontalScroller = nonDelayedHorizontalScrollView {
					onScrollChange {
						_, scrollX, _, _, _ ->
						viewModel.bottomScroller.scrollX = scrollX
					}
					linearLayout {
						orientation = HORIZONTAL
						repeat(STEPS_TO_ALLOCATE) {
							viewModel.elements.add(toneSequenceElement {
								viewModel = this@ToneSequenceUI.viewModel
							}.lparams {
								width = dimen(R.dimen.subdivision_controller_size)
								height = dip(1000f)
							})
						}
					}
					isHorizontalScrollBarEnabled = false
				}
			}.lparams {
				width = MATCH_PARENT
				height = MATCH_PARENT
				alignParentRight()
				above(viewModel.bottomScroller)
				rightOf(viewModel.leftScroller)
				if(configuration.portrait) {
					below(viewModel.topology)
				} else {
					alignParentTop()
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
				alignParentLeft()
				alignParentBottom()
			}
		}
	}
}
