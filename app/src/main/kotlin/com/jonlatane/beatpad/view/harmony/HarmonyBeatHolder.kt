package com.jonlatane.beatpad.view.harmony

import android.support.v7.widget.RecyclerView

class HarmonyBeatHolder(
	val viewModel: HarmonyViewModel,
	val element: HarmonyBeatView,
	private val adapter: HarmonyBeatAdapter
) : RecyclerView.ViewHolder(element) {
	private val context get() = element.context
}