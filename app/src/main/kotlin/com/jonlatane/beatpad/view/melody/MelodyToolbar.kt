package com.jonlatane.beatpad.view.melody

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Button
import android.widget.PopupMenu
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.service.convertPatternIndex
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.mod12
import com.jonlatane.beatpad.util.mod12Nearest
import com.jonlatane.beatpad.util.toolbarTextStyle
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick

class MelodyToolbar(
	context: Context,
	val viewModel: PaletteViewModel
): _LinearLayout(context), AnkoLogger {
	init {
		orientation = HORIZONTAL
		backgroundColor = context.color(R.color.colorPrimaryDark)
	}

	val melodyViewModel get() = viewModel.melodyViewModel

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
		toolbarTextStyle()
	}.lparams {
		width = matchParent
		height = wrapContent
		weight = 1f
	}
	private val relativeToMenu = PopupMenu(context, relativeToButton).also {
		it.inflate(R.menu.melody_relative_menu)
		it.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.fixedPositionMelody -> { melodyViewModel.openedMelody?.apply {
						if(shouldConformWithHarmony) {
              viewModel.harmonyViewModel.harmony?.let { harmony ->
                transposeOutOf(harmony)
              } ?: {
                transposeInPlace(viewModel.orbifold.chord.root.mod12Nearest)
              }()
						}
						shouldConformWithHarmony = false
            tonic = 0
					}
				}
				R.id.relativeToCurrentChord -> melodyViewModel.openedMelody?.apply {
          //TODO: Base this off harmony chord if in a Section with Harmony
					val newTonic = viewModel.orbifold.chord.root.mod12
          if(!shouldConformWithHarmony) {
            viewModel.harmonyViewModel.harmony?.let {harmony ->
                transposeInPlace(harmony)
            } ?: {
                transposeInPlace((tonic - newTonic).mod12Nearest)
            }()
          }
					shouldConformWithHarmony = true
					tonic = newTonic
				}
				R.id.c -> melodyViewModel.openedMelody?.apply {
					shouldConformWithHarmony = true
					tonic = 0
				}
				R.id.cSharp -> melodyViewModel.openedMelody?.apply {
					shouldConformWithHarmony = true
					tonic = 1
				}
				R.id.d -> melodyViewModel.openedMelody?.apply {
					shouldConformWithHarmony = true
					tonic = 2
				}
				R.id.dSharp -> melodyViewModel.openedMelody?.apply {
					shouldConformWithHarmony = true
					tonic = 3
				}
				R.id.e -> melodyViewModel.openedMelody?.apply {
					shouldConformWithHarmony = true
					tonic = 4
				}
				R.id.f -> melodyViewModel.openedMelody?.apply {
					shouldConformWithHarmony = true
					tonic = 5
				}
				R.id.fSharp -> melodyViewModel.openedMelody?.apply {
					shouldConformWithHarmony = true
					tonic = 6
				}
				R.id.g -> melodyViewModel.openedMelody?.apply {
					shouldConformWithHarmony = true
					tonic = 7
				}
				R.id.gSharp -> melodyViewModel.openedMelody?.apply {
					shouldConformWithHarmony = true
					tonic = 8
				}
				R.id.a -> melodyViewModel.openedMelody?.apply {
					shouldConformWithHarmony = true
					tonic = 9
				}
				R.id.aSharp -> melodyViewModel.openedMelody?.apply {
					shouldConformWithHarmony = true
					tonic = 10
				}
				R.id.b -> melodyViewModel.openedMelody?.apply {
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
		relativeToButton.text = melodyViewModel.openedMelody?.let {
			when {
				!it.shouldConformWithHarmony -> "Fixed Position"
				else -> "Relative to ${Chord.mod12Names[it.tonic.mod12]}"
			}
		} ?: "oops"


		lengthButton.text = "${melodyViewModel.openedMelody?.length}/${melodyViewModel.openedMelody?.subdivisionsPerBeat}"
		lengthDialog.lengthPicker.value = melodyViewModel.openedMelody?.length ?: 1
		lengthDialog.subdivisionsPerBeatPicker.value = melodyViewModel.openedMelody?.subdivisionsPerBeat ?: 1
	}
	private fun updateMelody() = viewModel.melodyElementAdapter?.notifyDataSetChanged()

  private fun Melody<*>.transposeInPlace(interval: Int) {
    when(this) {
      is RationalMelody -> {
        val transposed = transpose(interval)
        transposed.changes.forEach { (index, element) ->
          changes[index] = element
        }
      }
      else -> TODO("Melody type cannot be transposed!")
    }
  }

  private fun Melody<*>.transposeInPlace(harmony: Harmony) {
    when(this) {
      is RationalMelody -> {
        changes.forEach { (index, element) ->
          val harmonyPosition = index.convertPatternIndex(this, harmony)
          val chordInHarmony = harmony.changeBefore(harmonyPosition)
          val interval = (tonic - chordInHarmony.root).mod12Nearest
          val transposed = element.transpose(interval)
          changes[index] = transposed
        }
      }
      else -> TODO("Melody type cannot be transposed!")
    }
  }

  private fun Melody<*>.transposeOutOf(harmony: Harmony) {
    when(this) {
      is RationalMelody -> {
        changes.forEach { (index, element) ->
          val harmonyPosition = index.convertPatternIndex(this, harmony)
          val chordInHarmony = harmony.changeBefore(harmonyPosition)
          val interval = (chordInHarmony.root - tonic).mod12Nearest + tonic.mod12Nearest
          val transposed = element.transpose(chordInHarmony.root.mod12Nearest)
          changes[index] = transposed
        }
      }
      else -> TODO("Melody type cannot be transposed!")
    }
  }

	private val upButton = button {
		text = "Up"
		onClick {
      melodyViewModel.openedMelody?.transposeInPlace(1)
			updateMelody()
		}
		onLongClick(returnValue = true) {
      melodyViewModel.openedMelody?.transposeInPlace(12)
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
      melodyViewModel.openedMelody?.transposeInPlace(-1)
			updateMelody()
		}
		onLongClick(returnValue = true) {
      melodyViewModel.openedMelody?.transposeInPlace(-12)
			context.toast("Octave Down")
			updateMelody()
		}
	}.lparams {
		width = matchParent
		height = wrapContent
		weight = 1f
	}
}