package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Section
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.wrapContent

class SectionListAdapter(
	val viewModel: PaletteViewModel
) : RecyclerView.Adapter<SectionHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionHolder {
		return SectionHolder(parent)
	}

	override fun onBindViewHolder(holder: SectionHolder, position: Int) {
		if(position < viewModel.palette.sections.size) {
			val section = viewModel.palette.sections[position]
			holder.textView.apply {
				text = section.name
				backgroundResource = when {
//					c.isDominant -> R.drawable.orbifold_chord_dominant
//					c.isDiminished -> R.drawable.orbifold_chord_diminished
//					c.isMinor -> R.drawable.orbifold_chord_minor
//					c.isAugmented -> R.drawable.orbifold_chord_augmented
//					c.isMajor -> R.drawable.orbifold_chord_major
					else -> R.drawable.orbifold_chord
				}
				setOnClickListener {
					//viewModel.orbifold.disableNextTransitionAnimation()
					//viewModel.orbifold.chord = c
					viewModel.harmonyViewModel.harmony = section.harmony
				}
				setOnLongClickListener {
					viewModel.palette.sections.removeAt(position)
					notifyItemRemoved(position)
					notifyItemRangeChanged(
						position,
						viewModel.palette.sections.size - position
					)
					true
				}
			}
		} else {
			makeAddButton(holder)
		}
		holder.textView.requestLayout()
	}

	fun makeAddButton(holder: SectionHolder) {
		holder.textView.apply {
			text = "+"
			backgroundResource = R.drawable.orbifold_chord
			setOnClickListener {
				viewModel.palette.sections.add(Section.forList(viewModel.palette.sections))
				notifyItemInserted(viewModel.palette.sections.size - 1)
			}
			setOnLongClickListener {
				viewModel.palette.sections.add(Section.forList(viewModel.palette.sections))
				notifyItemInserted(viewModel.palette.sections.size)
				true
			}
		}
	}

	override fun getItemCount(): Int = viewModel.palette.sections.size + 1
}