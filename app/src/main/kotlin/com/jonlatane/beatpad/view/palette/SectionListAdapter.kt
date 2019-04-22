package com.jonlatane.beatpad.view.palette

import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.MotionEvent
import android.view.ViewGroup
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.storage.PaletteStorage.blankHarmony
import java.util.*

class SectionListAdapter(
	val viewModel: PaletteViewModel
) : RecyclerView.Adapter<SectionHolder>() {
  val recyclerView: RecyclerView get() = viewModel.sectionListRecycler
  val itemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.Callback() {
    init {
    }
    override fun getMovementFlags(
      recyclerView: RecyclerView?,
      viewHolder: RecyclerView.ViewHolder
    ): Int {
      val dragFlags = ItemTouchHelper.START or ItemTouchHelper.END
      return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
      recyclerView: RecyclerView?,
      viewHolder: RecyclerView.ViewHolder?,
      target: RecyclerView.ViewHolder?
    ): Boolean {
      if(viewHolder == null || target == null) return false
      val fromPosition = viewHolder.adapterPosition
      val toPosition = target.adapterPosition
      if(toPosition >= viewModel.palette.sections.size) return false
      if (fromPosition < toPosition) {
        for (i in fromPosition until toPosition) {
          Collections.swap(viewModel.palette.sections, i, i + 1)
        }
      } else {
        for (i in fromPosition downTo toPosition + 1) {
          Collections.swap(viewModel.palette.sections, i, i - 1)
        }
      }
      notifyItemMoved(fromPosition, toPosition)
      return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
      // TODO("not implemented")
    }

    override fun isLongPressDragEnabled(): Boolean = true
  }).also { it.attachToRecyclerView(recyclerView) }
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionHolder {
		return SectionHolder(parent, viewModel)
	}

	override fun onBindViewHolder(holder: SectionHolder, position: Int) {
    holder.invalidate()
		holder.sectionName.requestLayout()
		holder.dragHandle.setOnTouchListener { view, event ->
      if (MotionEventCompat.getActionMasked(event) ==
        MotionEvent.ACTION_DOWN) {
        itemTouchHelper.startDrag(holder)
      }
      false
    }
	}

  internal fun addSection() {
    viewModel.palette.sections.add(
      Section.forList(viewModel.palette.sections, harmony = blankHarmony)
    )
    notifyItemInserted (viewModel.palette.sections.size - 1)
  }

	override fun getItemCount(): Int = viewModel.palette.sections.size + 1
}