package com.jonlatane.beatpad.view.harmony

import android.support.v7.widget.RecyclerView
import android.widget.TextView

class ChordHolder(
	val viewModel: HarmonyViewModel,
	val element: TextView,
	private val adapter: ChordAdapter
) : RecyclerView.ViewHolder(element) {
	private val context get() = element.context
}