package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import com.jonlatane.beatpad.view.nonDelayedScrollView
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import com.jonlatane.beatpad.view.zoomableScrollView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.sdk25.coroutines.onScrollChange


inline fun ViewManager.melodyView(
	theme: Int = 0,
	viewModel: PaletteViewModel,

	//ui: AnkoContext<Any>,
	init: HideableRelativeLayout.() -> Unit
)
	= //with(ui) {
	ankoView({
		val melodyViewModel = viewModel.melodyViewModel
		melodyViewModel.melodyView = HideableRelativeLayout(it).apply {
			melodyViewModel.melodyToolbar = melodyToolbar(viewModel) {
				id = R.id.melody_toolbar
			}.lparams {
				width = matchParent
				height = wrapContent
				alignParentTop()
			}
			melodyViewModel.melodyEditingModifiers = melodyEditingModifiers {
				id = R.id.bottom_scroller
				onHeldDownChanged = { heldDown ->
					//if (heldDown) holdToEdit?.animate()?.alpha(0f)?.translationY(100f)
					//else holdToEdit?.animate()?.alpha(1f)?.translationY(0f)
          melodyViewModel.melodyCenterHorizontalScroller.scrollingEnabled = !heldDown
          melodyViewModel.melodyCenterVerticalScroller.scrollingEnabled = !heldDown
				}
			}.lparams {
				alignParentBottom()
				alignParentRight()
				width = ViewGroup.LayoutParams.MATCH_PARENT
				height = dimen(R.dimen.subdivision_controller_size)
				leftMargin = dip(30)
			}
      melodyViewModel.melodyLeftScroller = nonDelayedScrollView {
				id = R.id.left_scroller
				linearLayout {
          melodyViewModel.verticalAxis = melodyToneAxis().lparams {
						width = dip(30)
						height = dip(MelodyBeatAdapter.initialBeatHeightDp)
					}
				}
				scrollingEnabled = false
				isVerticalScrollBarEnabled = false
			}.lparams {
				width = dip(30)
				height = ViewGroup.LayoutParams.MATCH_PARENT
				below(melodyViewModel.melodyToolbar)
				above(melodyViewModel.melodyEditingModifiers)
				alignParentLeft()
			}
      melodyViewModel.melodyCenterVerticalScroller = zoomableScrollView {
				id = R.id.center_v_scroller
				onScrollChange { _, _, scrollY, _, _ ->
          melodyViewModel.melodyLeftScroller.scrollY = scrollY
				}

				zoomHandler = { xDelta, yDelta ->
					AnkoLogger<MelodyViewModel>().info("Zooming: xDelta=$xDelta, yDelta=$yDelta")
					when {
						(xDelta.toInt() != 0 || yDelta.toInt() != 0) -> {
							viewModel.melodyBeatAdapter?.apply {
								elementWidth += xDelta.toInt()
								elementHeight += (10f * yDelta).toInt()
								notifyDataSetChanged()
							}
							true
						}
						else -> false
					}
				}

        melodyViewModel.melodyCenterHorizontalScroller = nonDelayedRecyclerView {
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
					viewModel.melodyBeatAdapter = MelodyBeatAdapter(melodyViewModel, this)
					adapter = viewModel.melodyBeatAdapter
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
			}.lparams {
				width = ViewGroup.LayoutParams.MATCH_PARENT
				height = ViewGroup.LayoutParams.MATCH_PARENT
				alignParentRight()
				above(melodyViewModel.melodyEditingModifiers)
				rightOf(melodyViewModel.melodyLeftScroller)
				below(melodyViewModel.melodyToolbar)
			}

		}
		viewModel.melodyView
	}, theme, init)
//}

