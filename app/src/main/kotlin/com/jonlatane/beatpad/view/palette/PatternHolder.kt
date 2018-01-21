package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.widget.PopupMenu
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.model.Rest
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.toast
import kotlin.properties.Delegates

class PatternHolder(
	val viewModel: PaletteViewModel,
	val textView: TextView,
	val adapter: PatternAdapter,
  initialPattern: Int = 0
) : RecyclerView.ViewHolder(textView) {
	val partPosition get() = adapter.partPosition
	val part get() = adapter.part
	var patternPosition by Delegates.observable(initialPattern) {
		_, _, _ -> onPositionChanged()
	}
	val pattern get() = part.melodies[patternPosition]
	private val context get() = textView.context

	private val newPatternMenu = PopupMenu(textView.context, textView)
	private val editPatternMenu = PopupMenu(textView.context, textView)

	init {
		newPatternMenu.inflate(R.menu.pattern_new_menu)
		newPatternMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.newDrawnPattern ->
					viewModel.editingSequence = adapter.insert(newPattern())
				R.id.newMidiPattern -> context.toast("TODO!")
				R.id.newAudioPattern -> context.toast("TODO!")
				else -> context.toast("Impossible!")
			}
			true
		}
		editPatternMenu.inflate(R.menu.pattern_edit_menu)
		editPatternMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.editPattern -> viewModel.editingSequence = pattern
				R.id.removePattern -> context.toast("TODO!")
				else -> context.toast("TODO!")
			}
			true
		}
	}

	private fun newPattern() = RationalMelody((1..16).map { Rest() }.toMutableList(), 4)

	private fun onPositionChanged() {
		if(patternPosition < viewModel.palette.parts[partPosition].melodies.size) {
			editMode()
		} else {
			addMode()
		}
	}

	private fun editMode() {
		textView.apply {
			text = ""
			backgroundResource = R.drawable.orbifold_chord
			setOnClickListener {
				viewModel.editingSequence = pattern
			}
			setOnLongClickListener {
				editPatternMenu.show()
				true
			}
		}
	}

	private fun addMode() {
		textView.apply {
			text = "+"
			backgroundResource = R.drawable.orbifold_chord
			setOnClickListener {
				viewModel.editingSequence = adapter.insert(newPattern())
			}
			setOnLongClickListener {
				newPatternMenu.show()
				true
			}
		}
	}
}