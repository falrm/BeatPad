package com.jonlatane.beatpad.view.palette

import android.support.constraint.ConstraintSet
import android.text.TextUtils
import android.view.Gravity
import android.widget.SeekBar
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.recyclerview.v7.recyclerView

interface PartHolderLayout {
	fun _RecyclerView.partHolder(viewModel: PaletteViewModel, adapter: PartListAdapter): PartHolder {
		var partName: TextView? = null
		var partPatternRecycler: _RecyclerView? = null
		var seekBar: SeekBar? = null
		val layout = _ConstraintLayout(context).apply {
			partName = textView {
				id = R.id.part_name
				textSize = 20f
				singleLine = true
				ellipsize = TextUtils.TruncateAt.MARQUEE
				marqueeRepeatLimit = -1
				typeface = MainApplication.chordTypeface
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
						TOP to TOP of ConstraintSet.PARENT_ID,
						START to START of ConstraintSet.PARENT_ID,
						END to END of ConstraintSet.PARENT_ID
					)
				}
				partPatternRecycler!! {
					connect(
						TOP to BOTTOM of R.id.part_name,
						BOTTOM to BOTTOM of ConstraintSet.PARENT_ID,
						START to START of ConstraintSet.PARENT_ID,
						END to END of ConstraintSet.PARENT_ID
					)
				}
				seekBar!! {
					connect(
						TOP to TOP of R.id.part_name,
						BOTTOM to BOTTOM of R.id.part_name,
						START to START of R.id.part_name,
						END to END of R.id.part_name
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