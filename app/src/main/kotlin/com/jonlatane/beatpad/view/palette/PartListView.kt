package com.jonlatane.beatpad.view.palette

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewManager
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.view.HideableRecyclerView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView

class PartListView(
	context: Context,
	val viewModel: PaletteViewModel
): HideableRecyclerView(context) {
	init {
		val orientation = LinearLayoutManager.HORIZONTAL
		backgroundColor = context.color(R.color.colorPrimaryDark)
		layoutManager = LinearLayoutManager(context, orientation, false).apply {
			//isItemPrefetchEnabled = false
		}
		overScrollMode = View.OVER_SCROLL_NEVER
		adapter = PartListAdapter(viewModel, this)
		clipChildren = false
		clipToPadding = false}
}