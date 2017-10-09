package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity.*
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.LinearLayout
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.view.nonDelayedHorizontalScrollView
import com.jonlatane.beatpad.view.nonDelayedScrollView
import com.jonlatane.beatpad.view.tonesequence.*
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onScrollChange

inline fun ViewManager.chordListView(
	theme: Int = 0,
	viewModel: PaletteViewModel
) = chordListView(theme, viewModel, {})

inline fun ViewManager.chordListView(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: _RecyclerView.() -> Unit
) = ankoView({
	_RecyclerView(it).apply {
		val listAdapter = object : RecyclerView.Adapter<ChordHolder>() {
			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChordHolder? {
				return ChordHolder(TextView(parent.context).apply {
					textSize = 25f
					background = context.getDrawable(R.drawable.orbifold_chord)
					layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)
					minimumWidth = context.dip(90)
					isClickable = true
					isLongClickable = true
					gravity = CENTER_VERTICAL or CENTER_HORIZONTAL
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

		val orientation = LinearLayoutManager.HORIZONTAL
		backgroundColor = resources.getColor(R.color.colorPrimaryDark)
		layoutManager = LinearLayoutManager(context, orientation, false)
		overScrollMode = View.OVER_SCROLL_NEVER
		adapter = listAdapter
		adapter.registerAdapterDataObserver(
			object : RecyclerView.AdapterDataObserver() {
				override fun onItemRangeInserted(start: Int, count: Int) {
					//updateEmptyViewVisibility(this@recyclerView)
				}

				override fun onItemRangeRemoved(start: Int, count: Int) {
					//updateEmptyViewVisibility(this@recyclerView)
				}
			})
	}
}, theme, init)