package com.jonlatane.beatpad.view.palette

import android.support.constraint.ConstraintSet
import android.text.TextUtils
import android.view.Gravity
import android.widget.SeekBar
import android.widget.TextView
import com.jonlatane.beatpad.R
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.recyclerview.v7.recyclerView

interface PartHolderLayout {
	fun _RecyclerView.partHolder(viewModel: PaletteViewModel, adapter: PartListAdapter): PartHolder {
		var partName: TextView? = null
		var partPatternRecycler: _RecyclerView? = null
		var seekBar: SeekBar? = null
		val layout = constraintLayout {
			partName = textView {
				id = R.id.part_name
				textSize = 20f
				singleLine = true
				ellipsize = TextUtils.TruncateAt.MARQUEE
				marqueeRepeatLimit = -1
				isSelected = true
				gravity = Gravity.CENTER_HORIZONTAL
				backgroundResource = R.drawable.part_background
				elevation = 1f
			}.lparams(matchParent, wrapContent)

			partPatternRecycler = recyclerView {
				id = R.id.part_patterns
				elevation = 1f
			}.lparams(matchParent, 0) as _RecyclerView

			seekBar = seekBar {
				id = R.id.part_volume
				elevation = 2f
				alpha = 0f
				max = 127
			}.lparams(matchParent, 0)

			applyConstraintSet {
				partName!! {
					connect(
						ConstraintSetBuilder.Side.TOP to ConstraintSetBuilder.Side.TOP of ConstraintSet.PARENT_ID,
						ConstraintSetBuilder.Side.START to ConstraintSetBuilder.Side.START of ConstraintSet.PARENT_ID,
						ConstraintSetBuilder.Side.END to ConstraintSetBuilder.Side.END of ConstraintSet.PARENT_ID
					)
				}
				partPatternRecycler!! {
					connect(
						ConstraintSetBuilder.Side.TOP to ConstraintSetBuilder.Side.BOTTOM of R.id.part_name,
						ConstraintSetBuilder.Side.BOTTOM to ConstraintSetBuilder.Side.BOTTOM of ConstraintSet.PARENT_ID,
						ConstraintSetBuilder.Side.START to ConstraintSetBuilder.Side.START of ConstraintSet.PARENT_ID,
						ConstraintSetBuilder.Side.END to ConstraintSetBuilder.Side.END of ConstraintSet.PARENT_ID
					)
				}
				seekBar!! {
					connect(
						ConstraintSetBuilder.Side.TOP to ConstraintSetBuilder.Side.TOP of R.id.part_name,
						ConstraintSetBuilder.Side.BOTTOM to ConstraintSetBuilder.Side.BOTTOM of R.id.part_name,
						ConstraintSetBuilder.Side.START to ConstraintSetBuilder.Side.START of R.id.part_name,
						ConstraintSetBuilder.Side.END to ConstraintSetBuilder.Side.END of R.id.part_name
					)
				}
			}
		}.lparams {
			width = dip(120)
			height = matchParent
		}
		return PartHolder(
			viewModel,
			layout,
			partPatternRecycler!!,
			partName!!,
			seekBar!!,
			adapter
		)
	}
}