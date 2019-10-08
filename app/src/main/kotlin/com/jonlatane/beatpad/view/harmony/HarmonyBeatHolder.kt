package com.jonlatane.beatpad.view.harmony

import android.support.v7.widget.RecyclerView
import com.jonlatane.beatpad.util.smartrecycler.SmartAdapter
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class HarmonyBeatHolder(
	val viewModel: HarmonyViewModel,
	val element: HarmonyBeatView,
	private val adapter: HarmonyBeatAdapter
) : RecyclerView.ViewHolder(element), SmartAdapter.Holder, AnkoLogger {
	private val context get() = element.context

	override fun updateSmartHolder() {
		element.run { beatPosition = beatPosition }
		element.invalidate()
	}
}