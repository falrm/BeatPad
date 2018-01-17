package com.jonlatane.beatpad.view.harmony

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.TextView
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.melody.MelodyElementAdapter
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView


inline fun ViewManager.harmonyView(
	theme: Int = 0,
	viewModel: MelodyViewModel,

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

		}
		viewModel.toneSequenceView
	}, theme, init)
//}

