package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.jonlatane.beatpad.R
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.recyclerview.v7.recyclerView


class PartListAdapter(
	val viewModel: PaletteViewModel,
	private val recyclerView: _RecyclerView
) : RecyclerView.Adapter<PartHolder>() {
	companion object {
		val MAX_PARTS = 5
	}
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartHolder? {
		return recyclerView.run {
			var partName: TextView? = null
			var partPatternRecycler: _RecyclerView? = null
			val layout = linearLayout() {
				orientation = LinearLayout.VERTICAL
				partName = textView {
					textSize = 20f
					singleLine = true
					ellipsize = TextUtils.TruncateAt.MARQUEE
					marqueeRepeatLimit = -1
					isSelected = true
					gravity = Gravity.CENTER_HORIZONTAL
					backgroundResource = R.drawable.part_background
				}.lparams {
					width = matchParent
					height = wrapContent
					//alignParentTop()
				}

				partPatternRecycler = recyclerView().lparams {
					//below(partName!!)
					height = matchParent
					width = matchParent
					weight = 1f
				} as _RecyclerView
				isClickable = true
				isLongClickable = true
				gravity = Gravity.CENTER_HORIZONTAL
				layoutParams
			}.lparams {
				width = dip(120)
				height = matchParent
			}

			PartHolder(viewModel, layout, partPatternRecycler!!, partName!!, this@PartListAdapter)
		}
	}

	override fun onBindViewHolder(holder: PartHolder, partPosition: Int) {
		holder.partPosition = partPosition
	}

	override fun getItemCount(): Int = when {
		viewModel.palette.parts.size < MAX_PARTS -> viewModel.palette.parts.size + 1
		else -> viewModel.palette.parts.size
	}
}