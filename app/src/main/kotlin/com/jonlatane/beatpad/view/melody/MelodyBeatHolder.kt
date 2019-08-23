package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.jonlatane.beatpad.util.smartrecycler.SmartAdapter
import com.jonlatane.beatpad.view.harmony.HarmonyBeatView
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.recyclerview.v7._RecyclerView

class MelodyBeatHolder constructor(
	val viewModel: MelodyViewModel,
	val melodyBeatView: MelodyBeatView,
	val harmonyBeatView: HarmonyBeatView,
	val element: View = _LinearLayout(
		melodyBeatView.context
	).apply {
		orientation = LinearLayout.VERTICAL
		addView(harmonyBeatView)
		addView(melodyBeatView)
	},
	private val adapter: MelodyBeatAdapter
) : RecyclerView.ViewHolder(element), SmartAdapter.Holder {
	override fun updateSmartHolder() {
		harmonyBeatView.invalidate()
		melodyBeatView.invalidate()
	}

	private val context get() = melodyBeatView.context
}