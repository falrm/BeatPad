package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewManager
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.recyclerview.v7._RecyclerView

fun ViewManager.sectionListView(
	theme: Int = 0,
	viewModel: PaletteViewModel
) = sectionListView(theme, viewModel, {})

inline fun ViewManager.sectionListView(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: _RecyclerView.() -> Unit
) = ankoView({
	_RecyclerView(it).apply {
		viewModel.sectionListAdapter = SectionListAdapter(viewModel)

		backgroundColor = context.color(R.color.colorPrimaryDark)
		layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).apply {
			isItemPrefetchEnabled = false
		}
		overScrollMode = View.OVER_SCROLL_NEVER
		adapter = viewModel.sectionListAdapter
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
}, theme, init)