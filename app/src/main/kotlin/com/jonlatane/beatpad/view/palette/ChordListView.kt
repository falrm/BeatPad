package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.Gravity.CENTER_VERTICAL
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.dip
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.wrapContent

fun ViewManager.chordListView(
	theme: Int = 0,
	viewModel: PaletteViewModel
) = chordListView(theme, viewModel, {})

inline fun ViewManager.chordListView(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: _RecyclerView.() -> Unit
) = ankoView({
	_RecyclerView(it).apply {
		viewModel.chordListAdapter = ChordListAdapter(viewModel)

		backgroundColor = context.color(R.color.colorPrimaryDark)
		layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).apply {
			isItemPrefetchEnabled = false
		}
		overScrollMode = View.OVER_SCROLL_NEVER
		adapter = viewModel.chordListAdapter
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