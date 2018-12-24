package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.PopupMenu
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.storage.PaletteStorage
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.toast
import org.jetbrains.anko.wrapContent
import kotlin.properties.Delegates

class MelodyHolder(
	val viewModel: PaletteViewModel,
	val adapter: MelodyAdapter,
	val layout: MelodyReferenceView = adapter.recyclerView.run {
    MelodyReferenceView(context).lparams {
			width = matchParent
      height = wrapContent
		}
	},
	initialMelody: Int = 0
) : RecyclerView.ViewHolder(layout) {
	val partPosition: Int get() = adapter.partPosition
	val part: Part get() = adapter.part
	var melodyPosition: Int by Delegates.observable(initialMelody) {
		_, _, _ -> onPositionChanged()
	}
	val pattern get() = part.melodies[melodyPosition]
  private val context get() = adapter.recyclerView.context
  lateinit var blah: View

	private val newPatternMenu = PopupMenu(layout.context, layout)
	private val editPatternMenu = PopupMenu(layout.context, layout)

	init {
		newPatternMenu.inflate(R.menu.part_melody_new_menu)
		newPatternMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.newDrawnPattern ->
					viewModel.editingMelody = adapter.insert(newPattern())
				R.id.newMidiPattern -> context.toast("TODO!")
				R.id.newAudioPattern -> context.toast("TODO!")
				else -> context.toast("Impossible!")
			}
			true
		}
		editPatternMenu.inflate(R.menu.part_melody_edit_menu)
		editPatternMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.editPattern -> viewModel.editingMelody = pattern
				R.id.removePattern -> showConfirmDialog(
					context,
					promptText = "Really delete this melody?",
					yesText = "Yes, delete melody"
				) {
					part.melodies.removeAt(melodyPosition)
					adapter.notifyItemRemoved(melodyPosition)
					adapter.notifyItemRangeChanged(
						melodyPosition,
            part.melodies.size - melodyPosition
					)
				}
				else -> context.toast("TODO!")
			}
			true
		}
	}

	private fun newPattern() = PaletteStorage.baseMelody

	private fun onPositionChanged() {
		if(melodyPosition < viewModel.palette.parts[partPosition].melodies.size) {
			editMode()
		} else {
			addMode()
		}
	}

	private fun editMode() {
		layout.name.apply {
      text = ""
			backgroundResource = R.drawable.orbifold_chord
			setOnClickListener {
				viewModel.editingMelody = pattern
			}
			setOnLongClickListener {
				editPatternMenu.show()
				true
			}
		}
	}

	private fun addMode() {
		layout.name.apply {
			text = "+"
			backgroundResource = R.drawable.orbifold_chord
			setOnClickListener {
				viewModel.editingMelody = adapter.insert(newPattern())
			}
			setOnLongClickListener {
				newPatternMenu.show()
				true
			}
		}
	}
}