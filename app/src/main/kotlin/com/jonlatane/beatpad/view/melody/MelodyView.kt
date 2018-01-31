package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.LinearLayout
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import com.jonlatane.beatpad.view.nonDelayedScrollView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.sdk25.coroutines.onScrollChange


inline fun ViewManager.melodyView(
	theme: Int = 0,
	viewModel: MelodyViewModel,

	//ui: AnkoContext<Any>,
	init: HideableRelativeLayout.() -> Unit
)
	= //with(ui) {
	ankoView({
		viewModel.melodyView = HideableRelativeLayout(it).apply {
			var holdToEdit: TextView? = null
			viewModel.melodyBottomScroller = bottomScroller {
				id = R.id.bottom_scroller
				onHeldDownChanged = { heldDown ->
					if (heldDown) holdToEdit?.animate()?.alpha(0f)?.translationY(100f)
					else holdToEdit?.animate()?.alpha(1f)?.translationY(0f)
					viewModel.melodyCenterHorizontalScroller.scrollingEnabled = !heldDown
					viewModel.melodyCenterVerticalScroller.scrollingEnabled = !heldDown
				}
				linearLayout {
					orientation = LinearLayout.HORIZONTAL
					repeat(MelodyUI.STEPS_TO_ALLOCATE) {
						viewModel.bottoms.add(
							view {
								background = context.getDrawable(R.drawable.tone_footer)
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
				width = ViewGroup.LayoutParams.MATCH_PARENT
				height = dimen(R.dimen.subdivision_controller_size)
				leftMargin = dip(30)
			}
			holdToEdit = textView {
				text = "Hold To Edit"
				textSize = 15f
				gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
			}.lparams {
				alignParentBottom()
				alignParentRight()
				width = ViewGroup.LayoutParams.MATCH_PARENT
				height = dimen(R.dimen.subdivision_controller_size)
				leftMargin = dip(30)
			}
			viewModel.melodyLeftScroller = nonDelayedScrollView {
				id = R.id.left_scroller
				linearLayout {
					viewModel.verticalAxis = toneSequenceAxis().lparams {
						width = dip(30)
						height = dip(1000f)
					}
				}
				scrollingEnabled = false
				isVerticalScrollBarEnabled = false
			}.lparams {
				width = dip(30)
				height = ViewGroup.LayoutParams.MATCH_PARENT
				above(viewModel.melodyBottomScroller)
				alignParentLeft()
			}
			viewModel.melodyCenterVerticalScroller = nonDelayedScrollView {
				id = R.id.center_v_scroller
				onScrollChange { _, _, scrollY, _, _ ->
					viewModel.melodyLeftScroller.scrollY = scrollY
				}

				viewModel.melodyCenterHorizontalScroller = nonDelayedRecyclerView {
					id = R.id.center_h_scroller
					isFocusableInTouchMode = true
				}.lparams {
					height = wrapContent
					width = matchParent
				}.apply {
					layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).apply {
						isItemPrefetchEnabled = false
					}
					overScrollMode = View.OVER_SCROLL_NEVER
					viewModel.melodyElementAdapter = MelodyElementAdapter(viewModel, this)
					adapter = viewModel.melodyElementAdapter
					adapter.registerAdapterDataObserver(
						object : RecyclerView.AdapterDataObserver() {
							override fun onItemRangeInserted(start: Int, count: Int) {
								//updateEmptyViewVisibility(this@recyclerView)
							}

							override fun onItemRangeRemoved(start: Int, count: Int) {
								//updateEmptyViewVisibility(this@recyclerView)
							}
						})
				}
				/*viewModel.melodyCenterHorizontalScroller = nonDelayedHorizontalScrollView {
					onScrollChange { _, scrollX, _, _, _ ->
						viewModel.melodyBottomScroller.scrollX = scrollX
					}
					linearLayout {
						orientation = LinearLayout.HORIZONTAL
						repeat(MelodyUI.STEPS_TO_ALLOCATE) {
							viewModel.elements.add(toneSequenceElement {
								this.viewModel = viewModel
							}.lparams {
								width = dimen(R.dimen.subdivision_controller_size)
								height = dip(1000f)
							})
						}
					}
					isHorizontalScrollBarEnabled = false
				}*/
			}.lparams {
				width = ViewGroup.LayoutParams.MATCH_PARENT
				height = ViewGroup.LayoutParams.MATCH_PARENT
				alignParentRight()
				above(viewModel.melodyBottomScroller)
				rightOf(viewModel.melodyLeftScroller)
				alignParentTop()
			}

		}
		viewModel.melodyView
	}, theme, init)
//}

