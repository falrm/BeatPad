package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jonlatane.beatpad.midi.GM1Effects
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.util.applyToHolders
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import kotlin.properties.Delegates.observable


class PartListAdapter(
	val viewModel: PaletteViewModel,
	private val recyclerView: _RecyclerView
) : RecyclerView.Adapter<PartHolder>(), PartHolderLayout {
	companion object {
		const val MAX_PARTS = 5
	}
	var editingVolume by observable(false) { _, _, editingVolume ->
		recyclerView.applyToHolders<PartHolder> { it.editingVolume = editingVolume }
	}

	init {
		viewModel.partListAdapter = this
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartHolder? {
		return recyclerView.partHolder(viewModel, this)
	}

	override fun onBindViewHolder(holder: PartHolder, partPosition: Int) {
		holder.partPosition = partPosition
		holder.editingVolume = editingVolume
	}

	override fun getItemCount(): Int = when {
		viewModel.palette.parts.size < MAX_PARTS -> viewModel.palette.parts.size + 1
		else -> viewModel.palette.parts.size
	}

	fun addPart() {
		viewModel.palette.parts.add(
			Part(
				GM1Effects.randomInstrument(
					channel = viewModel.palette.parts.size.toByte(),
					exceptions = viewModel.palette.parts.mapNotNull {
						(it.instrument as? MIDIInstrument)?.instrument
					}.toSet()
				)
			)
		)
		if (canAddParts()) {
			notifyItemInserted(viewModel.palette.parts.size - 1)
			notifyItemChanged(viewModel.palette.parts.size)
		} else {
			notifyItemChanged(viewModel.palette.parts.size - 1)
		}
	}

	fun canAddParts() = viewModel.palette.parts.size < PartListAdapter.MAX_PARTS
}