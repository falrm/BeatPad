package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.recyclerview.v7.recyclerView


class PartListAdapter(
	val viewModel: PaletteViewModel,
	private val recyclerView: _RecyclerView
) : RecyclerView.Adapter<PartHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartHolder? {
		return recyclerView.run {
			var partName: TextView? = null
			var partPatternRecycler: _RecyclerView? = null
			var idSeq = 1
			val layout = _RelativeLayout(parent.context).apply {
				partName = textView {
					id = idSeq++
					textSize = 20f
					singleLine = true
					ellipsize = TextUtils.TruncateAt.MARQUEE
					marqueeRepeatLimit = -1
					isSelected = true
					gravity = Gravity.CENTER_HORIZONTAL
					backgroundResource = R.drawable.orbifold_chord
				}.lparams {
					width = matchParent
					height = wrapContent
					alignParentTop()
				}

				partPatternRecycler = (recyclerView{ id = idSeq++ } as _RecyclerView).lparams {
					below(partName!!)
					height = matchParent
					width = matchParent
				}
				backgroundColor = context.color(R.color.colorPrimaryDark)
				isClickable = true
				isLongClickable = true
				gravity = Gravity.CENTER_HORIZONTAL
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

	override fun getItemCount(): Int = viewModel.palette.parts.size + 1
}