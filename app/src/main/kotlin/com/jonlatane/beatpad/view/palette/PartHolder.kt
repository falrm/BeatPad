package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.showInstrumentPicker
import com.jonlatane.beatpad.util.color
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.info
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener
import org.jetbrains.anko.toast
import kotlin.properties.Delegates
import kotlin.properties.Delegates.observable

class PartHolder(
	val viewModel: PaletteViewModel,
	val layout: ViewGroup,
	internal val melodyRecycler: _RecyclerView,
	internal val partName: TextView,
	private val volumeSeekBar: SeekBar,
	private val adapter: PartListAdapter,
	initialPart: Int = 0
) : RecyclerView.ViewHolder(layout), AnkoLogger {
	var partPosition: Int by Delegates.observable(initialPart) { _, _, _ -> onPartPositionChanged() }
	val part: Part?  get() = viewModel.palette.parts.getOrNull(partPosition)
	val context get() = melodyRecycler.context
	private val melodyReferenceAdapter = MelodyReferenceAdapter(viewModel, melodyRecycler, 0)
	var editingVolume: Boolean by observable(false) { _, _, editingVolume: Boolean ->
		if(editingVolume && isEditablePart) {
			volumeSeekBar.animate().alpha(1f)
			volumeSeekBar.isEnabled = true
			partName.isClickable = false
			partName.isLongClickable = false
			partName.animate().alpha(0.5f)
		} else {
			volumeSeekBar.animate().alpha(0f)
			volumeSeekBar.isEnabled = false
			partName.isClickable = true
			partName.isLongClickable = true
			partName.animate().alpha(1f)
		}
    melodyReferenceAdapter.boundViewHolders.forEach {
      info("hihi")
			if (editingVolume && !it.isAddButton) {
				it.animateMixOn()
			} else {
				it.animateMixOff()
			}
		}
	}

	private val editPartMenu = PopupMenu(partName.context, partName)

	init {
		editPartMenu.inflate(R.menu.part_edit_menu)
		editPartMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
			//R.id.newDrawnPattern -> adapter.newToneSequence()
				R.id.editPartInstrument -> editInstrument()
				R.id.usePartOnColorboard -> {
					viewModel.colorboardPart = part
					context.toast("Applied ${part?.instrument?.instrumentName} to Colorboard!")
				}
				R.id.usePartOnKeyboard -> {
					viewModel.keyboardPart = part
					context.toast("Applied ${part?.instrument?.instrumentName} to Keyboard!")
				}
				R.id.usePartOnSplat -> {
					viewModel.splatPart = part
					context.toast("Applied ${part?.instrument?.instrumentName} to Splat!")
				}
				R.id.removePart -> showConfirmDialog(
						context,
						promptText = "Really delete the ${part?.instrument?.instrumentName} part?",
						yesText = "Yes, delete part"
					) {
						viewModel.palette.parts.removeAt(partPosition)
						adapter.notifyItemRemoved(partPosition)
						adapter.notifyDataSetChanged()
					}
				else -> context.toast("TODO!")
			}
			true
		}
		melodyRecycler.adapter = melodyReferenceAdapter
	}

	fun editInstrument() {
		val instrument = part?.instrument
		when (instrument) {
			is MIDIInstrument ->
				showInstrumentPicker(instrument, layout.context) {
					adapter.notifyItemChanged(partPosition)
				}
		}
	}

  val isEditablePart: Boolean
		get() = partPosition < viewModel.palette.parts.size || !adapter.canAddParts()

	private fun onPartPositionChanged() {
		melodyReferenceAdapter.partPosition = partPosition
		if(isEditablePart) {
			makeEditablePart(partPosition)
		} else {
			makeAddButton()
		}
	}

	private fun makeEditablePart(partPosition: Int) {
		partName.apply {
			text = part!!.instrument.instrumentName
			setOnClickListener {
				editPartMenu.show()
			}
			setOnLongClickListener {
				editInstrument()
				true
			}
			if(viewModel.editingMix) {
				alpha = 0.5f
        isClickable = false
        isLongClickable = false
			}
		}
		melodyRecycler.apply {
			visibility = View.VISIBLE
			val orientation = LinearLayoutManager.VERTICAL
			backgroundColor = context.color(R.color.colorPrimaryDark)
			layoutManager = LinearLayoutManager(context, orientation, false)
			overScrollMode = View.OVER_SCROLL_NEVER
		}
    melodyReferenceAdapter.partPosition = partPosition
    melodyReferenceAdapter.notifyDataSetChanged()
		volumeSeekBar.apply {
      if(viewModel.editingMix) {
        alpha = 1f
        isEnabled = true
      } else {
        alpha = 0f
        isEnabled = false
      }
			//isIndeterminate = false
			progress = (part!!.volume * 127).toInt()
			onSeekBarChangeListener {
				onProgressChanged { _, progress, _ ->
					info("Setting part volume to ${progress.toFloat() / 127f}")
					part?.volume = progress.toFloat() / 127f
				}
			}
		}
	}

	private fun makeAddButton() {
		partName.apply {
			text = "+"
			setOnClickListener {
				adapter.addPart()
			}
			setOnLongClickListener { true }
		}
		melodyRecycler.apply {
			visibility = View.GONE
		}
		volumeSeekBar.apply {
			//isIndeterminate = true
			onSeekBarChangeListener {}
		}
	}
}