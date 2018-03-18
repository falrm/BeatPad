package com.jonlatane.beatpad.view.palette

import android.support.constraint.ConstraintSet.PARENT_ID
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.midi.GM1Effects
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import kotlin.properties.Delegates.observable


class PartListAdapter(
	val viewModel: PaletteViewModel,
	private val recyclerView: _RecyclerView
) : RecyclerView.Adapter<PartHolder>() {
	companion object {
		val MAX_PARTS = 5
	}
	var editingVolume by observable(false) { _, _, editingVolume ->
		(0 until recyclerView.childCount)
			.map { recyclerView.getChildViewHolder(recyclerView.getChildAt(it)) as PartHolder }
			.forEach { it.editingVolume = editingVolume }
	}

	init {
		viewModel.partListAdapter = this
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartHolder? {
		return recyclerView.run {
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
							TOP to TOP of PARENT_ID,
							START to START of PARENT_ID,
							END to END of PARENT_ID
						)
					}
					partPatternRecycler!! {
						connect(
							TOP to BOTTOM of R.id.part_name,
							BOTTOM to BOTTOM of PARENT_ID,
							START to START of PARENT_ID,
							END to END of PARENT_ID
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

			PartHolder(
				viewModel,
				layout,
				partPatternRecycler!!,
				partName!!,
				seekBar!!,
				this@PartListAdapter
			)
		}
	}

	override fun onBindViewHolder(holder: PartHolder, partPosition: Int) {
		holder.partPosition = partPosition
		holder.editingVolume = editingVolume
	}

	override fun getItemCount(): Int = when {
		viewModel.palette.parts.size < MAX_PARTS -> viewModel.palette.parts.size + 1
		else -> viewModel.palette.parts.size
	}

	fun addPart() {
		viewModel.palette.parts.add(
			Part(
				GM1Effects.randomInstrument(
					channel = viewModel.palette.parts.size.toByte(),
					exceptions = viewModel.palette.parts.mapNotNull {
						(it.instrument as? MIDIInstrument)?.instrument
					}.toSet()
				)
			)
		)
		if (canAddParts())
			notifyItemInserted(viewModel.palette.parts.size - 1)
		else
			notifyItemChanged(viewModel.palette.parts.size - 1)
	}

	fun canAddParts() = viewModel.palette.parts.size < PartListAdapter.MAX_PARTS
}