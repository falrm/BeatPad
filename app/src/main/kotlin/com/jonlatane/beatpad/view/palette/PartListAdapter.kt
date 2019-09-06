package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.ViewGroup
import com.jonlatane.beatpad.BuildConfig
import com.jonlatane.beatpad.midi.GM1Effects
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.util.smartrecycler.SmartAdapter
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import java.util.*


class PartListAdapter(
	val viewModel: PaletteViewModel,
	internal val recyclerView: _RecyclerView
) : SmartAdapter<PartHolder>() {
	companion object {
		val MAX_PARTS: Int = when(BuildConfig.FLAVOR) {
			"full" -> 8
			else -> 5
		}
	}

	init {
		viewModel.partListAdapters.add(this)
	}


	val itemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.Callback() {
		init {
		}
		override fun getMovementFlags(
			recyclerView: RecyclerView,
			viewHolder: RecyclerView.ViewHolder
		): Int {
			return makeMovementFlags(ItemTouchHelper.START or ItemTouchHelper.END, 0)
		}

		override fun onMove(
			recyclerView: RecyclerView,
			viewHolder: RecyclerView.ViewHolder,
			target: RecyclerView.ViewHolder
		): Boolean {
			if(viewHolder == null || target == null) return false
			val fromPosition = viewHolder.adapterPosition
			val toPosition = target.adapterPosition
			(viewHolder as? PartHolder)?.editPartMenu?.dismiss()
			val rightMostPosition = if(canAddParts()) viewModel.palette.parts.size
				else viewModel.palette.parts.size + 1
			if(toPosition >= rightMostPosition || fromPosition >= rightMostPosition) return false
			if (fromPosition < toPosition) {
				for (i in fromPosition until toPosition) {
					Collections.swap(viewModel.palette.parts, i, i + 1)
				}
			} else {
				for (i in fromPosition downTo toPosition + 1) {
					Collections.swap(viewModel.palette.parts, i, i - 1)
				}
			}
			notifyItemMoved(fromPosition, toPosition)
			notifyItemChanged(fromPosition)
			notifyItemChanged(toPosition)
			return true
		}

		override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
			// TODO("not implemented")
		}

		override fun isLongPressDragEnabled(): Boolean = true
	}).also { it.attachToRecyclerView(recyclerView) }


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