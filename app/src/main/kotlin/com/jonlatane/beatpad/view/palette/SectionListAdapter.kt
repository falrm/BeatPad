package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.storage.PaletteStorage.blankHarmony

class SectionListAdapter(
	val viewModel: PaletteViewModel
) : RecyclerView.Adapter<SectionHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionHolder {
		return SectionHolder(parent, viewModel)
	}

	override fun onBindViewHolder(holder: SectionHolder, position: Int) {
    holder.invalidate()
		holder.sectionName.requestLayout()
	}

  internal fun addSection() {
    viewModel.palette.sections.add(
      Section.forList(viewModel.palette.sections, harmony = blankHarmony)
    )
    notifyItemInserted (viewModel.palette.sections.size - 1)
  }

	override fun getItemCount(): Int = viewModel.palette.sections.size + 1
}