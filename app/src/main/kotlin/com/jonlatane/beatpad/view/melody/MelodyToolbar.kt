package com.jonlatane.beatpad.view.melody

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.PopupMenu
import android.widget.RelativeLayout
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.mod12
import com.jonlatane.beatpad.util.mod12Nearest
import com.jonlatane.beatpad.util.toolbarStyle
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick

class MelodyToolbar(
	context: Context,
	val viewModel: MelodyViewModel
): _LinearLayout(context), AnkoLogger {
	init {
		orientation = HORIZONTAL
		backgroundColor = context.color(R.color.colorPrimaryDark)
	}

  private val lengthDialog = LengthDialog(context)

	private val lengthButton: Button = button {
		text = "0/0"
		onClick {
			//context.toast("TODO")
      try {
        lengthDialog.show()
      } catch(t: Throwable) {
        error("Error showing length dialog", t)
      }
		}
	}.lparams {
		width = matchParent
		height = wrapContent
		weight = 1f
	}

	private val relativeToButton: Button = button {
		text = ""
		onClick {
			relativeToMenu.show()
		}
		toolbarStyle()
	}.lparams {
		width = matchParent
		height = wrapContent
		weight = 1f
	}
	private val relativeToMenu = PopupMenu(context, relativeToButton).also {
		it.inflate(R.menu.melody_relative_menu)
		it.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.fixedPositionMelody -> { viewModel.openedMelody.apply {
						if(shouldConformWithHarmony) {
							transposeInPlace(viewModel.orbifold.chord.root.mod12Nearest)
						}
						shouldConformWithHarmony = false
						tonic = 0
					}
				}
				R.id.relativeToCurrentChord -> viewModel.openedMelody.apply {
					val newRoot = viewModel.orbifold.chord.root.mod12
					if(!shouldConformWithHarmony) {
						transposeInPlace((tonic - newRoot).mod12Nearest)
					}
					shouldConformWithHarmony = true
					tonic = newRoot
				}
				R.id.c -> viewModel.openedMelody.apply {
					shouldConformWithHarmony = true
					tonic = 0
				}
				R.id.cSharp -> viewModel.openedMelody.apply {
					shouldConformWithHarmony = true
					tonic = 1
				}
				R.id.d -> viewModel.openedMelody.apply {
					shouldConformWithHarmony = true
					tonic = 2
				}
				R.id.dSharp -> viewModel.openedMelody.apply {
					shouldConformWithHarmony = true
					tonic = 3
				}
				R.id.e -> viewModel.openedMelody.apply {
					shouldConformWithHarmony = true
					tonic = 4
				}
				R.id.f -> viewModel.openedMelody.apply {
					shouldConformWithHarmony = true
					tonic = 5
				}
				R.id.fSharp -> viewModel.openedMelody.apply {
					shouldConformWithHarmony = true
					tonic = 6
				}
				R.id.g -> viewModel.openedMelody.apply {
					shouldConformWithHarmony = true
					tonic = 7
				}
				R.id.gSharp -> viewModel.openedMelody.apply {
					shouldConformWithHarmony = true
					tonic = 8
				}
				R.id.a -> viewModel.openedMelody.apply {
					shouldConformWithHarmony = true
					tonic = 9
				}
				R.id.aSharp -> viewModel.openedMelody.apply {
					shouldConformWithHarmony = true
					tonic = 10
				}
				R.id.b -> viewModel.openedMelody.apply {
					shouldConformWithHarmony = true
					tonic = 11
				}
			}
			updateButtonText()
			updateMelody()
			true
		}
	}

	@SuppressLint("SetTextI18n")
	fun updateButtonText() {
		relativeToButton.text = when {
			!viewModel.openedMelody.shouldConformWithHarmony -> "Fixed Position"
			else -> "Relative to ${Chord.mod12Names[viewModel.openedMelody.tonic.mod12]}"
		}

		lengthButton.text =
			"${viewModel.openedMelody.elements.size}/${viewModel.openedMelody.subdivisionsPerBeat}"
    lengthDialog.lengthPicker.value = viewModel.openedMelody.elements.size
    lengthDialog.subdivisionsPerBeatPicker.value = viewModel.openedMelody.subdivisionsPerBeat
	}
	private fun updateMelody() = viewModel.melodyElementAdapter?.notifyDataSetChanged()


	private fun Melody.transposeInPlace(interval: Int) {
		val transposed = transpose(interval)
		transposed.elements.forEachIndexed { index, element ->
			elements[index] = element
		}
	}
	private val upButton = button {
		text = "Up"
		onClick {
			viewModel.openedMelody.transposeInPlace(1)
			updateMelody()
		}
		onLongClick {
			viewModel.openedMelody.transposeInPlace(12)
			context.toast("Octave Up")
			updateMelody()
		}
	}.lparams {
		width = matchParent
		height = wrapContent
		weight = 1f
	}

	private val downButton = button {
		text = "Down"
		onClick {
			viewModel.openedMelody.transposeInPlace(-1)
			updateMelody()
		}
		onLongClick {
			viewModel.openedMelody.transposeInPlace(-12)
			context.toast("Octave Down")
			updateMelody()
		}
	}.lparams {
		width = matchParent
		height = wrapContent
		weight = 1f
	}
}