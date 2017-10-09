package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.transition.Visibility
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.RationalToneSequence
import com.jonlatane.beatpad.model.ToneSequence
import com.jonlatane.beatpad.showInstrumentPicker
import com.jonlatane.beatpad.view.palette.PartHolder.Companion.instrumentTextId
import com.jonlatane.beatpad.view.palette.PartHolder.Companion.sequenceRecyclerId
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.recyclerview.v7.recyclerView

inline fun ViewManager.partListView(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: _RecyclerView.() -> Unit
): _RecyclerView = ankoView({
	_RecyclerView(it).apply {
		val partListAdapter = object : RecyclerView.Adapter<PartHolder>() {
			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartHolder? {
				return PartHolder(_RelativeLayout(parent.context).apply {
					val name = textView {
						id = instrumentTextId
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
					//TODO: Make this a list of sequences for the Part
					recyclerView {
						id = sequenceRecyclerId
					}.lparams {
						below(name)
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
				})
			}

			override fun onBindViewHolder(holder: PartHolder, partPosition: Int) {
				if(partPosition < viewModel.palette.parts.size) {
					val part = viewModel.palette.parts[partPosition]
					holder.instrumentText.apply {
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
					holder.recycler.apply {
						visibility = View.VISIBLE
						val sequenceListAdapter = object : RecyclerView.Adapter<PartSequenceHolder>() {
							override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartSequenceHolder? {
								return PartSequenceHolder(TextView(parent.context).apply {
									textSize = 25f
									background = context.getDrawable(R.drawable.orbifold_chord)
									layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)
									minimumWidth = context.dip(90)
									isClickable = true
									isLongClickable = true
									gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
								}.lparams {
									width = matchParent
								})
							}

							override fun onBindViewHolder(holder: PartSequenceHolder, sequencePosition: Int) {
								if(sequencePosition < viewModel.palette.parts[partPosition].segments.size) {
									val c = viewModel.palette.parts[partPosition].segments[sequencePosition]
									holder.textView.apply {
										text = ""
										backgroundResource = R.drawable.orbifold_chord
										setOnClickListener {
										}
										setOnLongClickListener {
											true
										}
									}
								} else {
									makeAddButton(holder)
								}
							}

							fun makeAddButton(holder: PartSequenceHolder) {
								holder.textView.apply {
									text = "+"
									backgroundResource = R.drawable.orbifold_chord
									setOnClickListener {
										viewModel.palette.parts[partPosition].segments.add(RationalToneSequence())
										notifyItemInserted(viewModel.palette.parts[partPosition].segments.size - 1)
									}
									setOnLongClickListener {
										true
									}
								}
							}

							override fun getItemCount(): Int = viewModel.palette.parts[partPosition].segments.size + 1
						}
						val orientation = LinearLayoutManager.VERTICAL
						backgroundColor = resources.getColor(R.color.colorPrimaryDark)
						layoutManager = LinearLayoutManager(context, orientation, false)
						overScrollMode = View.OVER_SCROLL_NEVER
						adapter = sequenceListAdapter
					}
				} else {
					makeAddButton(holder)
				}
			}

			fun makeAddButton(holder: PartHolder) {
				holder.instrumentText.apply {
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
				holder.recycler.apply {
					visibility = View.GONE
				}
			}

			override fun getItemCount(): Int = viewModel.palette.parts.size + 1
		}

		val orientation = LinearLayoutManager.HORIZONTAL
		backgroundColor = resources.getColor(R.color.colorPrimaryDark)
		layoutManager = LinearLayoutManager(context, orientation, false)
		overScrollMode = View.OVER_SCROLL_NEVER
		adapter = partListAdapter
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