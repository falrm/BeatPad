package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jonlatane.beatpad.R
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView


class PatternElementAdapter(
	val viewModel: PatternViewModel,
	val recyclerView: _RecyclerView
) : RecyclerView.Adapter<PatternElementHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatternElementHolder? {
		return with(recyclerView) {
			PatternElementHolder(
				viewModel = viewModel,
				element = toneSequenceElement {
					this@toneSequenceElement.viewModel = this@PatternElementAdapter.viewModel
				}.lparams {
					width = dimen(R.dimen.subdivision_controller_size)
					height = dip(1000f)
				},
				adapter = this@PatternElementAdapter
			)
		}
	}

	override fun onBindViewHolder(holder: PatternElementHolder, elementPosition: Int) {
		holder.element.elementPosition = elementPosition
		holder.element.invalidate()
	}

	override fun getItemCount(): Int = viewModel.toneSequence.elements.size
}