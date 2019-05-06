package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.showInstrumentPicker2
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.vibrate
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener
import kotlin.properties.Delegates.observable

class PartHolder(
	val viewModel: PaletteViewModel,
	val layout: PartHolderView,
	private val adapter: PartListAdapter
) : RecyclerView.ViewHolder(layout), PartHolderLayout, AnkoLogger {
	val part: Part?  get() = viewModel.palette.parts.getOrNull(adapterPosition)
	val context get() = layout.context
	private val melodyReferenceAdapter = MelodyReferenceAdapter(viewModel, layout.melodyReferenceRecycler, 0)
	val volumeSlider: SeekBar get() = layout.volumeSlider
	val partName: TextView get() = layout.partName
	var editingVolume: Boolean by observable(false) { _, _, editingVolume: Boolean ->
		if(editingVolume && isEditablePart) {
			volumeSlider.animate().alpha(1f)
			volumeSlider.isEnabled = true
			partName.isClickable = false
			partName.isLongClickable = false
			partName.animate().alpha(0.5f)
		} else {
			volumeSlider.animate().alpha(0f)
			volumeSlider.isEnabled = false
			partName.isClickable = (part?.instrument as? MIDIInstrument)?.drumTrack != true
			partName.isLongClickable = true
			partName.animate().alpha(1f)
		}
    melodyReferenceAdapter.boundViewHolders.forEach {
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
          viewModel.palette.parts.removeAt(adapterPosition)
          adapter.notifyItemRemoved(adapterPosition)
          adapter.notifyDataSetChanged()
        }
        else -> context.toast("TODO!")
      }
      true
    }
    newPartMenu.inflate(R.menu.part_new_menu)
    newPartMenu.setOnMenuItemClickListener { item ->
      when (item.itemId) {
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
		layout.melodyReferenceRecycler.adapter = melodyReferenceAdapter
	}

	fun editInstrument() {
		val instrument = part?.instrument
		when (instrument) {
			is MIDIInstrument ->
			showInstrumentPicker2(instrument, layout.context)
		}
	}

  val isEditablePart: Boolean
		get() = adapterPosition < viewModel.palette.parts.size || !adapter.canAddParts()

  fun onPartPositionChanged() {
    editingVolume = viewModel.editingMix
		melodyReferenceAdapter.partPosition = adapterPosition
		if(isEditablePart) {
			makeEditablePart()
		} else {
			makeAddButton()
		}
	}

	private fun makeEditablePart() {
    val drumTrack = (part!!.instrument as? MIDIInstrument)?.drumTrack == true
		partName.apply {
			text = part!!.instrument.instrumentName
      if((part?.instrument as? MIDIInstrument)?.drumTrack != true) {
        isClickable = true
        setOnClickListener {
          vibrate(10)
          editInstrument()
        }
      } else {
        setOnClickListener { }
        isClickable = false
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
			partNamePadding()
			if(viewModel.editingMix) {
				alpha = 0.5f
        isClickable = false
        isLongClickable = false
			}
		}
    layout.melodyReferenceRecycler.apply {
			val orientation = LinearLayoutManager.VERTICAL
			backgroundColor = context.color(R.color.colorPrimaryDark)
			layoutManager = LinearLayoutManager(context, orientation, false)
			overScrollMode = View.OVER_SCROLL_NEVER
		}
    melodyReferenceAdapter.notifyDataSetChanged()
		volumeSlider.apply {
      if(viewModel.editingMix) {
        alpha = 1f
        isEnabled = true
      } else {
        alpha = 0f
        isEnabled = false
      }
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
      backgroundResource = R.drawable.part_background
      partNamePadding()
		}
		volumeSlider.apply {
			onSeekBarChangeListener {}
		}
	}
}