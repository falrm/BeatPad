package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.PopupMenu
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.storage.PaletteStorage
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener
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
) : RecyclerView.ViewHolder(layout), AnkoLogger {
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

  val isAddButton: Boolean
    get() = melodyPosition >= viewModel.palette.parts[partPosition].melodies.size

	private fun onPositionChanged() {
    if(isAddButton) {
      addMode()
    } else {
      editMode()
    }
	}

  internal fun animateEditOn() {
    layout.apply {
      name.isClickable = false
      arrayOf(volume, inclusion).forEach {
        it.alpha = 0f
        it.isEnabled = true
        it.animate().alpha(1f).start()
      }
    }
  }

  internal fun animateEditOff() {
    layout.apply {
      name.isClickable = false
      arrayOf(volume, inclusion).forEach {
        it.animate().alpha(0f).withEndAction {
          it.isEnabled = false
        }.start()
      }
    }
  }


	private fun editMode() {
		layout.apply {
      arrayOf(volume, inclusion).forEach {
				if(viewModel.editingMix) {
          it.isEnabled = true
          it.alpha = 1f
        } else {
          it.isEnabled = false
          it.alpha = 0f
        }
			}
      val melodyReference = BeatClockPaletteConsumer.section?.melodies?.firstOrNull { it.melody == pattern }
      inclusion.apply {
        imageResource = if(melodyReference != null) {
          R.drawable.icons8_speaker_100
        } else {
          R.drawable.icons8_mute_100
        }
        onClick {
          if(melodyReference == null) {
            BeatClockPaletteConsumer.section?.melodies?.add(
              Section.MelodyReference(pattern, 1f, Section.PlaybackType.Indefinite)
            )
          } else {
            BeatClockPaletteConsumer.section?.melodies?.removeAll { it.melody == pattern }
          }
          editMode()
        }
      }
      if(melodyReference != null) {
        volume.isIndeterminate = false
        volume.progress = (melodyReference.volume * 127).toInt()
        volume.onSeekBarChangeListener {
          onProgressChanged { _, progress, _ ->
            info("Setting melody volume to ${progress.toFloat() / 127f}")
            melodyReference.volume = progress.toFloat() / 127f
          }
        }
      } else {
        volume.progress = 0
        volume.isIndeterminate = true
        volume.isEnabled = false
      }
			name.apply {
				text = ""
        isClickable = !viewModel.editingMix
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
	}

	private fun addMode() {
    layout.apply {
      arrayOf(volume, inclusion).forEach {
        it.alpha = 0f
        it.isEnabled = false
      }
      name.apply {
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
}