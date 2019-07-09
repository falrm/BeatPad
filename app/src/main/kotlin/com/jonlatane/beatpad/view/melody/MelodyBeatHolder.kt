package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.RecyclerView
import com.jonlatane.beatpad.util.smartrecycler.SmartAdapter

class MelodyBeatHolder(
	val viewModel: MelodyViewModel,
	val element: MelodyBeatView,
	private val adapter: MelodyBeatAdapter
) : RecyclerView.ViewHolder(element), SmartAdapter.Holder {
	override fun updateSmartHolder() {
		element.invalidate()
	}

	private val context get() = element.context
}