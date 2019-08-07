package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.ViewGroup
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.storage.PaletteStorage.blankHarmony
import com.jonlatane.beatpad.util.smartrecycler.SmartAdapter
import java.util.*

class SectionListAdapter(
	val viewModel: PaletteViewModel,
  val recyclerView: RecyclerView
) : SmartAdapter<SectionHolder>() {
  val orientation: Int get() = (recyclerView.layoutManager as? LinearLayoutManager)?.orientation
    ?: LinearLayoutManager.HORIZONTAL

  val itemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.Callback() {
    init {
    }
    override fun getMovementFlags(
      recyclerView: RecyclerView?,
      viewHolder: RecyclerView.ViewHolder
    ): Int {
      val dragFlags = when(orientation) {
        LinearLayoutManager.HORIZONTAL -> ItemTouchHelper.START or ItemTouchHelper.END
        else -> ItemTouchHelper.UP or ItemTouchHelper.DOWN
      }
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
      (viewHolder as? SectionHolder)?.menu?.dismiss()
      if(toPosition >= viewModel.palette.sections.size || fromPosition >= viewModel.palette.sections.size)
        return false
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
      notifyItemChanged(fromPosition)
      notifyItemChanged(toPosition)
      if(
        listOf(viewHolder, target).mapNotNull { (it as? SectionHolder)?.section }
          .any { it == BeatClockPaletteConsumer.section }
      ) {
        viewModel.beatScratchToolbar.updateButtonColors()
        //viewModel.partListAdapter?.updateSmartHolders()
        viewModel.notifySectionChange()
      }
      return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
      // TODO("not implemented")
    }

    override fun isLongPressDragEnabled(): Boolean = true
  }).also { it.attachToRecyclerView(recyclerView) }
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionHolder {
		return SectionHolder(orientation, parent, viewModel)
	}

	override fun onBindViewHolder(holder: SectionHolder, position: Int) {
    holder.updateSmartHolder()
	}

  internal fun addSection(
    section: Section = Section.forList(viewModel.palette.sections, harmony = blankHarmony),
    position: Int = viewModel.palette.sections.size
  ) {
    viewModel.palette.sections.add(position, section)
    notifyItemInserted(position)
//    viewModel.sectionListAdapter?.notifyItemRangeChanged(
//      position,
//      itemCount - position
//    )
  }

	override fun getItemCount(): Int = viewModel.palette.sections.size + 1
}