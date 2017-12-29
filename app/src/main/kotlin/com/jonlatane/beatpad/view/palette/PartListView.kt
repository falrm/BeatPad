package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewManager
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.view.HideableRecyclerView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.partListView(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: HideableRecyclerView.() -> Unit
): HideableRecyclerView = ankoView({
	HideableRecyclerView(it).apply {
		val orientation = LinearLayoutManager.HORIZONTAL
		backgroundColor = context.color(R.color.colorPrimaryDark)
		layoutManager = LinearLayoutManager(context, orientation, false)
		overScrollMode = View.OVER_SCROLL_NEVER
		adapter = PartListAdapter(viewModel, this)
//		adapter.registerAdapterDataObserver(
//			object : RecyclerView.AdapterDataObserver() {
//				override fun onItemRangeInserted(start: Int, count: Int) {
//					//updateEmptyViewVisibility(this@recyclerView)
//				}
//
//				override fun onItemRangeRemoved(start: Int, count: Int) {
//					//updateEmptyViewVisibility(this@recyclerView)
//				}
//			})
	}
}, theme, init)