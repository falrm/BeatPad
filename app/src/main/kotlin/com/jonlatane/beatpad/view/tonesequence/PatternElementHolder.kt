package com.jonlatane.beatpad.view.tonesequence

import android.support.v7.widget.RecyclerView
import kotlin.properties.Delegates

class PatternElementHolder(
	val viewModel: PatternViewModel,
	private val element: PatternElementView,
	private val adapter: PatternElementAdapter,
	initialPosition: Int = 0
) : RecyclerView.ViewHolder(element) {
	private val context get() = element.context
	var elementPosition: Int by Delegates.observable(initialPosition) { _, _, new ->
		element.seqIndex = new
		element.invalidate()
	}
	init {
		element.viewModel = viewModel
	}
}