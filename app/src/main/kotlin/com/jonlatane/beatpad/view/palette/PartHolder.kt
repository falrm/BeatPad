package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.midi.GM1Effects
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.showInstrumentPicker
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.vibrate
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener
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
	private val newPartMenu = PopupMenu(partName.context, partName)

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
          if((part?.instrument as? MIDIInstrument)?.drumTrack == true)
            viewModel.keyboardView.ioHandler.highlightChord(null)
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
    newPartMenu.inflate(R.menu.part_new_menu)
    newPartMenu.setOnMenuItemClickListener { item ->
      when (item.itemId) {
        //R.id.newDrawnPattern -> adapter.newToneSequence()
        R.id.newMidiPart -> adapter.addPart()
        R.id.newMidiDrumPart -> {
          adapter.addPart(Part(
						MIDIInstrument(
							channel = 9.toByte(),
							drumTrack = true
						)
					))
        }
        R.id.newRecordedPart -> context.toast("TODO!")
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
    val drumTrack = (part!!.instrument as? MIDIInstrument)?.drumTrack == true
		partName.apply {
			text = part!!.instrument.instrumentName
			setOnClickListener {
				vibrate(10)
				editInstrument()
			}
      textColor = if (drumTrack) R.color.colorPrimaryLight else R.color.colorPrimaryDark
			setOnLongClickListener {
        arrayOf(R.id.editPartInstrument, R.id.usePartOnColorboard, R.id.usePartOnSplat).forEach {
          editPartMenu.menu.findItem(it).isEnabled = !drumTrack
        }
				editPartMenu.show()
				true
			}
			backgroundResource = if ((part!!.instrument as? MIDIInstrument)?.drumTrack == true)
				R.drawable.part_background_drum
			else R.drawable.part_background
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
          (part?.instrument as? MIDIInstrument)?.sendSelectInstrument()
				}
			}
		}
	}

	private fun makeAddButton() {
		partName.apply {
			text = "+"
			setOnClickListener {
				vibrate(10)
				adapter.addPart()
			}
			setOnLongClickListener {
        newPartMenu.menu.findItem(R.id.newMidiDrumPart).isEnabled = viewModel.palette.parts.none {
          (it.instrument as? MIDIInstrument)?.drumTrack == true
        }
        newPartMenu.show()
        true
      }
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