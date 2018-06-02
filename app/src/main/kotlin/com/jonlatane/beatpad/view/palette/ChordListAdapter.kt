package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.wrapContent

class ChordListAdapter(
	val viewModel: PaletteViewModel
) : RecyclerView.Adapter<ChordHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChordHolder? {
		return ChordHolder(TextView(parent.context).apply {
			textSize = 25f
			background = context.getDrawable(R.drawable.orbifold_chord)
			layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)
			minimumWidth = context.dip(90)
			isClickable = true
			isLongClickable = true
			gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
			typeface = MainApplication.chordTypeface
			setPadding(
				dip(50),
				dip(5),
				dip(50),
				dip(5)
			)
		})
	}

	override fun onBindViewHolder(holder: ChordHolder, position: Int) {
		if(position < viewModel.palette.chords.size) {
			val c = viewModel.palette.chords[position]
			holder.textView.apply {
				text = c.name
				backgroundResource = when {
					c.isDominant -> R.drawable.orbifold_chord_dominant
					c.isDiminished -> R.drawable.orbifold_chord_diminished
					c.isMinor -> R.drawable.orbifold_chord_minor
					c.isAugmented -> R.drawable.orbifold_chord_augmented
					c.isMajor -> R.drawable.orbifold_chord_major
					else -> R.drawable.orbifold_chord
				}
				setOnClickListener {
					viewModel.orbifold.disableNextTransitionAnimation()
					viewModel.orbifold.chord = c
				}
				setOnLongClickListener {
					viewModel.palette.chords.removeAt(position)
					notifyItemRemoved(position)
					notifyItemRangeChanged(
						position,
						viewModel.palette.chords.size - position
					)
					true
				}
			}
		} else {
			makeAddButton(holder)
		}
		holder.textView.requestLayout()
	}

	fun makeAddButton(holder: ChordHolder) {
		holder.textView.apply {
			text = "+"
			backgroundResource = R.drawable.orbifold_chord
			setOnClickListener {
				viewModel.palette.chords.add(viewModel.orbifold.chord)
				notifyItemInserted(viewModel.palette.chords.size - 1)
			}
			setOnLongClickListener {
				viewModel.palette.chords.add(viewModel.orbifold.chord)
				notifyItemInserted(viewModel.palette.chords.size)
				true
			}
		}
	}

	override fun getItemCount(): Int = viewModel.palette.chords.size + 1
}