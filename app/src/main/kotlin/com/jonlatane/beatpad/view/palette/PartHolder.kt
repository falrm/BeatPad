package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.widget.RelativeLayout
import android.widget.TextView
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import kotlin.properties.Delegates

class PartHolder(
	val viewModel: PaletteViewModel,
	val layout: RelativeLayout,
	val patternRecycler: _RecyclerView,
	val partName: TextView,
	initialPart: Int = 0
) : RecyclerView.ViewHolder(layout) {
	var partPosition by Delegates.observable(initialPart) {
		_, _, new -> patternAdapter.partPosition = new
	}
	val part get() = viewModel.palette.parts[partPosition]
	val patternAdapter = PatternAdapter(viewModel, patternRecycler, 0)

	init {
		patternRecycler.adapter = patternAdapter
	}
}