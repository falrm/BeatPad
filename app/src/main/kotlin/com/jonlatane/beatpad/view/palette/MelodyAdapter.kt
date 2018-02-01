package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Melody
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.wrapContent
import kotlin.properties.Delegates


class MelodyAdapter(
	val viewModel: PaletteViewModel,
	val recyclerView: _RecyclerView,
	initialPart: Int = 0
) : RecyclerView.Adapter<MelodyHolder>() {
	var partPosition by Delegates.observable(initialPart) {
		_, _, _ -> notifyDataSetChanged()
	}
	val part get() = viewModel.palette.parts[partPosition]

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyHolder? {
		return recyclerView.run {
			MelodyHolder(viewModel, TextView(parent.context).apply {
				textSize = 25f
				background = context.getDrawable(R.drawable.orbifold_chord)
				layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)
				minimumWidth = context.dip(90)
				isClickable = true
				isLongClickable = true
				gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
			}.lparams {
				width = matchParent
			}, this@MelodyAdapter)
		}
	}

	override fun onBindViewHolder(holder: MelodyHolder, patternPosition: Int) {
		holder.patternPosition = patternPosition
	}

	fun insert(sequence: Melody): Melody {
		part.melodies.add(sequence)
		notifyItemInserted(part.melodies.size - 1)
		return sequence
	}

	override fun getItemCount(): Int = viewModel.palette.parts[partPosition].melodies.size + 1
}