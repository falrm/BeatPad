package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.midi.GM1Effects
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.showInstrumentPicker
import com.jonlatane.beatpad.util.color
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.toast
import kotlin.properties.Delegates

class PartHolder(
	val viewModel: PaletteViewModel,
	val layout: ViewGroup,
	private val patternRecycler: _RecyclerView,
	private val partName: TextView,
	private val adapter: PartListAdapter,
	initialPart: Int = 0
) : RecyclerView.ViewHolder(layout) {
	var partPosition by Delegates.observable(initialPart) { _, _, _ -> onPartPositionChanged() }
	val part get() = viewModel.palette.parts[partPosition]
	val context get() = patternRecycler.context
	private val patternAdapter = MelodyAdapter(viewModel, patternRecycler, 0)

	private val editPartMenu = PopupMenu(partName.context, partName)

	init {
		editPartMenu.inflate(R.menu.part_edit_menu)
		editPartMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
			//R.id.newDrawnPattern -> adapter.newToneSequence()
				R.id.editPartInstrument -> editInstrument()
				R.id.usePartOnColorboard -> {
					viewModel.colorboardPart = part
					context.toast("Applied ${part.instrument.instrumentName} to Colorboard!")
				}
				R.id.usePartOnKeyboard -> {
					viewModel.keyboardPart = part
					context.toast("Applied ${part.instrument.instrumentName} to Keyboard!")
				}
				R.id.usePartOnSplat -> {
					viewModel.splatPart = part
					context.toast("Applied ${part.instrument.instrumentName} to Splat!")
				}
				R.id.removePart -> showConfirmDialog(
						context,
						promptText = "Really delete the ${part.instrument.instrumentName} part?",
						yesText = "Yes, delete part"
					) {
						viewModel.palette.parts.removeAt(partPosition)
						adapter.notifyItemRemoved(partPosition)
					}
				else -> context.toast("TODO!")
			}
			true
		}
		patternRecycler.adapter = patternAdapter
	}

	fun editInstrument() {
		val instrument = part.instrument
		when (instrument) {
			is MIDIInstrument ->
				showInstrumentPicker(instrument, layout.context) {
					adapter.notifyItemChanged(partPosition)
				}
		}
	}

	private fun onPartPositionChanged() {
		patternAdapter.partPosition = partPosition
		if (partPosition < viewModel.palette.parts.size || !adapter.canAddParts()) {
			makeEditablePart(partPosition)
		} else {
			makeAddButton()
		}
	}

	private fun makeEditablePart(partPosition: Int) {
		partName.apply {
			text = part.instrument.instrumentName
			setOnClickListener {
				editPartMenu.show()

			}
			setOnLongClickListener {
				editInstrument()
				true
			}
		}
		patternRecycler.apply {
			visibility = View.VISIBLE
			val sequenceListAdapter = MelodyAdapter(viewModel, this, partPosition)
			val orientation = LinearLayoutManager.VERTICAL
			backgroundColor = context.color(R.color.colorPrimaryDark)
			layoutManager = LinearLayoutManager(context, orientation, false)
			overScrollMode = View.OVER_SCROLL_NEVER
			adapter = sequenceListAdapter
		}
	}

	private fun makeAddButton() {
		partName.apply {
			text = "+"
			setOnClickListener {
				adapter.addPart()
			}
			setOnLongClickListener {
				//						viewModel.palette.chords.add(viewModel.orbifold.chord)
//						notifyItemInserted(viewModel.palette.parts.size)
				true
			}
		}
		patternRecycler.apply {
			visibility = View.GONE
		}
	}
}