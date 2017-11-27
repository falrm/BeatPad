package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.showInstrumentPicker
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.recyclerview.v7.recyclerView


class PartListAdapter(
	val viewModel: PaletteViewModel,
	val recyclerView: _RecyclerView
) : RecyclerView.Adapter<PartHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartHolder? {
		return recyclerView.run {
			var partName: TextView? = null
			var partPatternRecycler: _RecyclerView? = null
			val layout = _RelativeLayout(parent.context).apply {
				partName = textView {
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

				partPatternRecycler = (recyclerView() as _RecyclerView).lparams {
					below(partName!!)
					height = matchParent
					width = matchParent
				}
				backgroundColor = resources.getColor(R.color.colorPrimaryDark)
				isClickable = true
				isLongClickable = true
				gravity = Gravity.CENTER_HORIZONTAL
			}.lparams {
				width = dip(120)
				height = matchParent
			}

			PartHolder(viewModel, layout, partPatternRecycler!!, partName!!)
		}
	}

	override fun onBindViewHolder(holder: PartHolder, partPosition: Int) {
		if(partPosition < viewModel.palette.parts.size) {
			makeEditablePart(holder, partPosition)
		} else {
			makeAddButton(holder)
		}
	}

	fun makeEditablePart(holder: PartHolder, partPosition: Int) {
		val part = viewModel.palette.parts[partPosition]
		holder.partName.apply {
			text = part.instrument.instrumentName
			setOnClickListener {
			}
			setOnLongClickListener {
				showInstrumentPicker(part.instrument, context) {
					notifyItemChanged(partPosition)
				}
				true
			}
		}
		holder.patternRecycler.apply {
			visibility = View.VISIBLE
			val sequenceListAdapter = PatternAdapter(viewModel, this, partPosition)
			val orientation = LinearLayoutManager.VERTICAL
			backgroundColor = resources.getColor(R.color.colorPrimaryDark)
			layoutManager = LinearLayoutManager(context, orientation, false)
			overScrollMode = View.OVER_SCROLL_NEVER
			adapter = sequenceListAdapter
		}
	}

	fun makeAddButton(holder: PartHolder) {
		holder.partName.apply {
			text = "+"
			backgroundResource = R.drawable.orbifold_chord
			setOnClickListener {
				viewModel.palette.parts.add(Palette.Part())
				notifyItemInserted(viewModel.palette.parts.size - 1)
			}
			setOnLongClickListener {
				//						viewModel.palette.chords.add(viewModel.orbifold.chord)
//						notifyItemInserted(viewModel.palette.parts.size)
				true
			}
		}
		holder.patternRecycler.apply {
			visibility = View.GONE
		}
	}

	override fun getItemCount(): Int = viewModel.palette.parts.size + 1
}