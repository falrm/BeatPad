package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jonlatane.beatpad.R
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView


class MelodyElementAdapter(
	val viewModel: MelodyViewModel,
	val recyclerView: _RecyclerView
) : RecyclerView.Adapter<MelodyElementHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyElementHolder? {
		return with(recyclerView) {
			MelodyElementHolder(
				viewModel = viewModel,
				element = toneSequenceElement {
					this@toneSequenceElement.viewModel = this@MelodyElementAdapter.viewModel
				}.lparams {
					width = dimen(R.dimen.subdivision_controller_size)
					height = dip(1000f)
				},
				adapter = this@MelodyElementAdapter
			)
		}
	}

	override fun onBindViewHolder(holder: MelodyElementHolder, elementPosition: Int) {
		holder.element.elementPosition = elementPosition
		holder.element.invalidate()
	}

	override fun getItemCount(): Int = viewModel.openedMelody.elements.size
}