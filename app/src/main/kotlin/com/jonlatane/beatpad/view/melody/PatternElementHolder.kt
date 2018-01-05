package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.RecyclerView

class PatternElementHolder(
	val viewModel: PatternViewModel,
	val element: PatternElementView,
	private val adapter: PatternElementAdapter
) : RecyclerView.ViewHolder(element) {
	private val context get() = element.context
	init {
		element.viewModel = viewModel
	}
}