package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jonlatane.beatpad.model.Melody
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import kotlin.properties.Delegates


class MelodyReferenceAdapter(
	val viewModel: PaletteViewModel,
	val recyclerView: _RecyclerView,
	initialPart: Int = 0
) : RecyclerView.Adapter<MelodyReferenceHolder>() {
	var partPosition by Delegates.observable(initialPart) {
		_, _, _ -> notifyDataSetChanged()
	}
	val part get() = viewModel.palette.parts[partPosition]

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyReferenceHolder {
		return recyclerView.run {
			MelodyReferenceHolder(viewModel, this@MelodyReferenceAdapter)
		}
	}

	override fun onBindViewHolder(holder: MelodyReferenceHolder, patternPosition: Int) {
		holder.melodyPosition = patternPosition
	}

	fun insert(sequence: Melody<*>): Melody<*> {
		part.melodies.add(sequence)
		notifyItemInserted(part.melodies.size - 1)
		return sequence
	}

	override fun getItemCount(): Int = viewModel.palette.parts[partPosition].melodies.size + 1
}