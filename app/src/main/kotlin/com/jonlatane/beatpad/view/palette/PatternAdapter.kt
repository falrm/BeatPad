package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.RationalToneSequence
import com.jonlatane.beatpad.model.ToneSequence
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.wrapContent
import kotlin.properties.Delegates


class PatternAdapter(
	val viewModel: PaletteViewModel,
	val recyclerView: _RecyclerView,
	initialPart: Int = 0
) : RecyclerView.Adapter<PatternHolder>() {
	var partPosition by Delegates.observable(initialPart) {
		_, _, _ -> notifyDataSetChanged()
	}
	val part get() = viewModel.palette.parts[partPosition]

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatternHolder? {
		return recyclerView.run {
			PatternHolder(viewModel, TextView(parent.context).apply {
				textSize = 25f
				background = context.getDrawable(R.drawable.orbifold_chord)
				layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)
				minimumWidth = context.dip(90)
				isClickable = true
				isLongClickable = true
				gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
			}.lparams {
				width = matchParent
			}, this@PatternAdapter)
		}
	}

	override fun onBindViewHolder(holder: PatternHolder, patternPosition: Int) {
		holder.patternPosition = patternPosition
	}

	fun insert(sequence: ToneSequence): ToneSequence {
		part.segments.add(sequence)
		notifyItemInserted(part.segments.size - 1)
		return sequence
	}

	override fun getItemCount(): Int = viewModel.palette.parts[partPosition].segments.size + 1
}