package com.jonlatane.beatpad.view.palette

import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.jonlatane.beatpad.midi.GM1Effects
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.util.SmartAdapter
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7._RecyclerView


class PartListAdapter(
	val viewModel: PaletteViewModel,
	internal val recyclerView: _RecyclerView
) : SmartAdapter<PartHolder>() {
	companion object {
		const val MAX_PARTS = 8
	}

	init {
		viewModel.partListAdapter = this
	}



	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartHolder {
		return recyclerView.partHolder(viewModel, this)
	}



	fun _RecyclerView.partHolder(viewModel: PaletteViewModel, adapter: PartListAdapter): PartHolder {
		val layout = PartHolderView(context).lparams {
      width = dip(120)
      height = matchParent
    }
		return PartHolder(
			viewModel,
			layout,
			adapter
		)
	}

	override fun onBindViewHolder(holder: PartHolder, position: Int) {
    super.onBindViewHolder(holder, position)
		holder.onPartPositionChanged()
  }
	override fun getItemCount(): Int = when {
		viewModel.palette.parts.size < MAX_PARTS -> viewModel.palette.parts.size + 1
		else -> viewModel.palette.parts.size
	}

	fun addPart(part: Part = Part(
		GM1Effects.randomInstrument(
			channel = viewModel.palette.parts.size.toByte(),
			exceptions = viewModel.palette.parts.mapNotNull {
				(it.instrument as? MIDIInstrument)?.instrument
			}.toSet()
		)
	)) {
		viewModel.palette.parts.add(part)
		if (canAddParts()) {
			notifyItemInserted(viewModel.palette.parts.size - 1)
			notifyItemChanged(viewModel.palette.parts.size)
		} else {
			notifyItemChanged(viewModel.palette.parts.size - 1)
		}
	}

	fun canAddParts() = viewModel.palette.parts.size < PartListAdapter.MAX_PARTS
}