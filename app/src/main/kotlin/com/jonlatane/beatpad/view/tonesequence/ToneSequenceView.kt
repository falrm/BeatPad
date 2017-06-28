package com.jonlatane.beatpad.view.tonesequence

import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.SequenceEditorActivity
import com.jonlatane.beatpad.view.nonDelayedHorizontalScrollView
import com.jonlatane.beatpad.view.nonDelayedScrollView
import com.jonlatane.beatpad.view.topology.topologyView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onScrollChange
import org.jetbrains.anko.sdk25.coroutines.onTouch

class ToneSequenceView : AnkoComponent<SequenceEditorActivity> {
	override fun createView(ui: AnkoContext<SequenceEditorActivity>) = with(ui) {
		val ID_CONTROLS = 1
		var subdivisionControllers: LinearLayout
		relativeLayout {
			val topology = topologyView {
				id = 3
			}.lparams {
				width = MATCH_PARENT
				height = dip(210f)
				alignParentTop()
			}
			val bottomScroller = nonDelayedHorizontalScrollView {
				id = 1
				subdivisionControllers = linearLayout {
					orientation = HORIZONTAL
					repeat(50) {
						view {
							background = ctx.getDrawable(R.drawable.key_highlight_root)
						}.lparams {
							width = dimen(com.jonlatane.beatpad.R.dimen.subdivision_controller_size)
							height = dimen(com.jonlatane.beatpad.R.dimen.subdivision_controller_size)
						}
					}
				}
				scrollingEnabled = false
			}.lparams {
				alignParentBottom()
				width = MATCH_PARENT
				height = dimen(R.dimen.subdivision_controller_size)
				leftMargin = dip(30)
			}
			val leftScroller = nonDelayedScrollView {
				id = 2
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
				alignParentLeft()
				above(bottomScroller)
				below(topology)
			}
			nonDelayedScrollView {
				onScrollChange {
					_, _, scrollY, _, _ ->
					leftScroller.scrollY = scrollY
				}
				nonDelayedHorizontalScrollView {
					onScrollChange {
						_, scrollX, _, _, _ ->
						bottomScroller.scrollX = scrollX
					}
					linearLayout {
						orientation = HORIZONTAL
						repeat(50) {
							toneSequenceElement().lparams {
								width = dimen(com.jonlatane.beatpad.R.dimen.subdivision_controller_size)
								height = dip(1000f)
							}
						}
					}
					isHorizontalScrollBarEnabled = false
				}
			}.lparams {
				width = MATCH_PARENT
				height = MATCH_PARENT
				alignParentRight()
				above(bottomScroller)
				below(topology)
				rightOf(leftScroller)
			}
		}
	}
}