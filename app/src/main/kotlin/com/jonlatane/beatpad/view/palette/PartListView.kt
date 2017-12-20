package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.*
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.RationalToneSequence
import com.jonlatane.beatpad.showInstrumentPicker
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.view.HideableRecyclerView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.recyclerview.v7.recyclerView

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