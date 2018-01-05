package com.jonlatane.beatpad.view.harmony

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jonlatane.beatpad.R
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.textView


class ChordAdapter(
	val viewModel: HarmonyViewModel,
	val recyclerView: _RecyclerView
) : RecyclerView.Adapter<ChordHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChordHolder? {
		return with(recyclerView) {
			ChordHolder(
				viewModel = viewModel,
				element = textView {
					//this@toneSequenceElement.viewModel = this@ChordAdapter.viewModel
				}.lparams {
					width = dimen(R.dimen.subdivision_controller_size)
					height = dip(1000f)
				},
				adapter = this@ChordAdapter
			)
		}
	}

	override fun onBindViewHolder(holder: ChordHolder, elementPosition: Int) {
		//holder.element.elementPosition = elementPosition
		holder.element.invalidate()
	}

	override fun getItemCount(): Int = viewModel.harmony.elements.size
}