package com.jonlatane.beatpad.view.harmony

import android.support.v7.widget.RecyclerView
import android.widget.TextView

class HarmonyChordHolder(
	val viewModel: HarmonyViewModel,
	val element: HarmonyElementView,
	private val adapter: HarmonyChordAdapter
) : RecyclerView.ViewHolder(element) {
	private val context get() = element.context
}