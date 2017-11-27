package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.widget.PopupMenu
import android.widget.TextView
import com.jonlatane.beatpad.R
import org.jetbrains.anko.backgroundResource
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
	val pattern get() = part.segments[patternPosition]

	val newItemMenu = PopupMenu(textView.context, textView)
	val editItemMenu = PopupMenu(textView.context, textView)

	init {
		newItemMenu.inflate(R.menu.new_sequence_menu)
		newItemMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.newDrawnPart -> adapter.newSequence()
			}
			true
		}
		editItemMenu.inflate(R.menu.new_sequence_menu)
		editItemMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.newDrawnPart -> adapter.newSequence()
			}
			true
		}
	}

	private fun onPositionChanged() {
		if(patternPosition < viewModel.palette.parts[partPosition].segments.size) {
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
				editItemMenu.show()
			}
			setOnLongClickListener {
				true
			}
		}
	}

	private fun addMode() {
		textView.apply {
			text = "+"
			backgroundResource = R.drawable.orbifold_chord
			setOnClickListener {
				newItemMenu.show()
			}
			setOnLongClickListener {
				true
			}
		}
	}
}