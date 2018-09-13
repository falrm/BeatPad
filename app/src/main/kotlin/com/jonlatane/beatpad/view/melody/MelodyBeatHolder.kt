package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.RecyclerView

class MelodyBeatHolder(
	val viewModel: MelodyViewModel,
	val element: MelodyBeatView,
	private val adapter: MelodyBeatAdapter
) : RecyclerView.ViewHolder(element) {
	private val context get() = element.context
	init {
		element.viewModel = viewModel
	}
}