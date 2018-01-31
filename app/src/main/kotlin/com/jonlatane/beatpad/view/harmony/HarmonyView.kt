package com.jonlatane.beatpad.view.harmony

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.TextView
import com.jonlatane.beatpad.R
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
		viewModel.melodyView = HideableRelativeLayout(it).apply {
			var holdToEdit: TextView? = null
				viewModel.melodyCenterHorizontalScroller = nonDelayedRecyclerView {
					id = R.id.center_h_scroller
					isFocusableInTouchMode = true
				}.lparams {
				width = ViewGroup.LayoutParams.MATCH_PARENT
				height = ViewGroup.LayoutParams.MATCH_PARENT
				alignParentRight()
				above(viewModel.melodyBottomScroller)
				rightOf(viewModel.melodyLeftScroller)
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
		viewModel.melodyView
	}, theme, init)
//}

