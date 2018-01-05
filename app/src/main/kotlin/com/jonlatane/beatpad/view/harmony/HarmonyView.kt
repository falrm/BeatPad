package com.jonlatane.beatpad.view.harmony

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
import com.jonlatane.beatpad.view.melody.PatternElementAdapter
import com.jonlatane.beatpad.view.melody.PatternViewModel
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import com.jonlatane.beatpad.view.nonDelayedScrollView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.sdk25.coroutines.onScrollChange


inline fun ViewManager.harmonyView(
	theme: Int = 0,
	viewModel: PatternViewModel,

	//ui: AnkoContext<Any>,
	init: HideableRelativeLayout.() -> Unit
)
	= //with(ui) {
	ankoView({
		viewModel.toneSequenceView = HideableRelativeLayout(it).apply {
			var holdToEdit: TextView? = null
			var IDSeq = 1

				viewModel.centerHorizontalScroller = nonDelayedRecyclerView {
					id = IDSeq++
					isFocusableInTouchMode = true
				}.lparams {
				width = ViewGroup.LayoutParams.MATCH_PARENT
				height = ViewGroup.LayoutParams.MATCH_PARENT
				alignParentRight()
				above(viewModel.bottomScroller)
				rightOf(viewModel.leftScroller)
				alignParentTop()
				}.apply {
					layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).apply {
						isItemPrefetchEnabled = false
					}
					overScrollMode = View.OVER_SCROLL_NEVER
					viewModel.patternElementAdapter = PatternElementAdapter(viewModel, this)
					adapter = viewModel.patternElementAdapter
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

		}
		viewModel.toneSequenceView
	}, theme, init)
//}

