package com.jonlatane.beatpad.view.tonesequence

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.HORIZONTAL
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.SequenceEditorActivity
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.view.NonDelayedHorizontalScrollView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.nonDelayedHorizontalScrollView
import com.jonlatane.beatpad.view.nonDelayedScrollView
import com.jonlatane.beatpad.view.topology.TopologyView
import com.jonlatane.beatpad.view.topology.topologyView
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onScrollChange

class ToneSequenceUI : AnkoComponent<SequenceEditorActivity> {
	override fun createView(ui: AnkoContext<SequenceEditorActivity>) = with(ui) {
		var IDSeq = 1 // Literally just a source of View IDs to make Android happy.
		val viewModel = ToneSequenceViewModel()
		var topology: TopologyView?
		var leftScroller: NonDelayedScrollView?
		var bottomScroller: NonDelayedHorizontalScrollView?
		val elements = mutableListOf<ToneSequenceElement>()
		val bottoms  = mutableListOf<View>()
		val previewInstrument = MIDIInstrument().apply {
			channel = 4
			instrument = GeneralMidiConstants.SYNTH_BASS_1
		}
		val instrument = MIDIInstrument().apply {
			channel = 5
			instrument = GeneralMidiConstants.SYNTH_BASS_1
		}

		relativeLayout {
			topology = topologyView {
				id = IDSeq++
			}.lparams {
				height = dip(210f)
				if(configuration.portrait) {
					width = MATCH_PARENT
					alignParentTop()
				} else {
					width = dip(350f)
					alignParentLeft()
				}
			}
			bottomScroller = nonDelayedHorizontalScrollView {
				id = IDSeq++
				linearLayout {
					orientation = HORIZONTAL
					repeat(viewModel.viewCount) {
						bottoms.add(
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
					rightOf(topology!!)
				}
			}
			leftScroller = nonDelayedScrollView {
				id = IDSeq++
				linearLayout {
					view {
						background = ctx.getDrawable(R.drawable.vertical_keyboard)
					}.lparams {
						width = dip(30)
						height = dip(1000f)
					}
				}
				scrollingEnabled = false
				isVerticalScrollBarEnabled = false
			}.lparams {
				width = dip(30)
				height = MATCH_PARENT
				above(bottomScroller!!)
				if(configuration.portrait) {
					alignParentLeft()
					below(topology!!)
				} else {
					rightOf(topology!!)
				}
			}
			nonDelayedScrollView {
				onScrollChange {
					_, _, scrollY, _, _ ->
					leftScroller?.scrollY = scrollY
				}
				nonDelayedHorizontalScrollView {
					onScrollChange {
						_, scrollX, _, _, _ ->
						bottomScroller?.scrollX = scrollX
					}
					linearLayout {
						orientation = HORIZONTAL
						repeat(viewModel.viewCount) {
							elements.add(toneSequenceElement().lparams {
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
				above(bottomScroller!!)
				rightOf(leftScroller!!)
				if(configuration.portrait) {
					below(topology!!)
				} else {
					alignParentTop()
				}
			}
		}
	}
}
