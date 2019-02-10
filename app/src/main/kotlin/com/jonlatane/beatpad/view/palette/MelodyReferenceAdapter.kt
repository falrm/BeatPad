package com.jonlatane.beatpad.view.palette

import android.view.ViewGroup
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.util.SmartAdapter
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import kotlin.properties.Delegates


class MelodyReferenceAdapter(
	val viewModel: PaletteViewModel,
	val recyclerView: _RecyclerView,
	initialPart: Int = 0
) : SmartAdapter<MelodyReferenceHolder>() {
	var partPosition by Delegates.observable(initialPart) {
		_, _, _ -> notifyDataSetChanged()
	}
	val part: Part? get() = viewModel.palette.parts.getOrNull(partPosition)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyReferenceHolder {
		return recyclerView.run {
			MelodyReferenceHolder(viewModel, this@MelodyReferenceAdapter)
		}
	}

	override fun onBindViewHolder(holder: MelodyReferenceHolder, position: Int) {
		super.onBindViewHolder(holder, position)
		holder.melodyPosition = position
	}

	fun insert(sequence: Melody<*>): Melody<*> {
		part?.let { part ->
			part.melodies.add(sequence)
			notifyItemInserted(part.melodies.size - 1)
			notifyItemChanged(part.melodies.size)
		}
		return sequence
	}

	override fun getItemCount(): Int = viewModel.palette.parts.getOrNull(partPosition)?.melodies
		?.let { it.size + 1 } ?: 0
}