package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.RecyclerView

class MelodyElementHolder(
	val viewModel: MelodyViewModel,
	val element: MelodyElementView,
	private val adapter: MelodyElementAdapter
) : RecyclerView.ViewHolder(element) {
	private val context get() = element.context
	init {
		element.viewModel = viewModel
	}
}