package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.PopupMenu
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.storage.PaletteStorage
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener
import kotlin.properties.Delegates

class MelodyReferenceHolder(
  val viewModel: PaletteViewModel,
  val adapter: MelodyReferenceAdapter,
  val layout: MelodyReferenceView = adapter.recyclerView.run {
    MelodyReferenceView(context).lparams {
			width = matchParent
      height = wrapContent
		}
	},
  initialMelody: Int = 0
) : RecyclerView.ViewHolder(layout), AnkoLogger {
	private val partPosition: Int get() = adapter.partPosition
	val part: Part get() = adapter.part
	internal var melodyPosition: Int by Delegates.observable(initialMelody) {
		_, _, _ -> onPositionChanged()
	}
	internal val melody: Melody<*>? get() = part.melodies.getOrNull(melodyPosition)
  val melodyReference: Section.MelodyReference?
    get() = BeatClockPaletteConsumer.section?.melodies?.firstOrNull { it.melody == melody }
  private val context get() = adapter.recyclerView.context

	private val newPatternMenu = PopupMenu(layout.context, layout)
	private val editPatternMenu = PopupMenu(layout.context, layout)

	init {
		newPatternMenu.inflate(R.menu.part_melody_new_menu)
		newPatternMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.newDrawnPattern ->
					viewModel.editingMelody = adapter.insert(newMelody())
				R.id.newMidiPattern -> context.toast("TODO!")
				R.id.newAudioPattern -> context.toast("TODO!")
				else -> context.toast("Impossible!")
			}
			true
		}
		editPatternMenu.inflate(R.menu.part_melody_edit_menu)
		editPatternMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.editPattern -> viewModel.editingMelody = melody
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

	private fun newMelody() = PaletteStorage.baseMelody

  val isAddButton: Boolean
    get() = melodyPosition >= viewModel.palette.parts[partPosition].melodies.size

	internal fun onPositionChanged() {
    if(isAddButton) {
      addMode()
    } else {
      editMode()
    }
	}

  internal fun animateEditOn() {
    layout.apply {
      name.isClickable = false
      arrayOf(volume, inclusion).forEach { it: View ->
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

  val Section.MelodyReference.disabled get() = playbackType == Section.PlaybackType.Disabled


  internal fun enableMelodyReference() {
    if(melodyReference == null) {
      BeatClockPaletteConsumer.section?.melodies?.add(
        Section.MelodyReference(melody!!, 0.5f, Section.PlaybackType.Indefinite)
      )
    } else if(melodyReference!!.disabled) {
      melodyReference!!.playbackType = Section.PlaybackType.Indefinite
    }
  }

  internal fun disableMelodyReference() {
    melodyReference!!.playbackType = Section.PlaybackType.Disabled
  }

	private fun editMode() {
		layout.apply {
      volume.apply {
				if(viewModel.editingMix) {
          isEnabled = true
          alpha = 1f
        } else {
          isEnabled = false
          alpha = 0f
        }
			}
      inclusion.apply {
        imageResource = if(melodyReference == null || melodyReference!!.disabled) {
          R.drawable.icons8_mute_100
        } else {
          R.drawable.icons8_speaker_100
        }
        isEnabled = true
        alpha = 1f
        onClick {
          if(melodyReference == null || melodyReference!!.disabled) {
            enableMelodyReference()
          } else {
            disableMelodyReference()
          }
          editMode()
        }
      }
      volume.apply {
        if(melodyReference == null || melodyReference!!.disabled) {
          isIndeterminate = true
          isEnabled = false
        } else {
          isIndeterminate = false
          progress = (melodyReference!!.volume * 127).toInt()
          onSeekBarChangeListener {
            onProgressChanged { _, progress, _ ->
              info("Setting melody volume to ${progress.toFloat() / 127f}")
              melodyReference!!.volume = progress.toFloat() / 127f
            }
          }
        }
      }
			name.apply {
				text = ""
        isClickable = !viewModel.editingMix
				backgroundResource = R.drawable.orbifold_chord
				setOnClickListener {
					viewModel.editingMelody = melody
				}
				setOnLongClickListener {
					editPatternMenu.show()
					true
				}
        alpha = if(melodyReference == null || melodyReference!!.disabled) 0.5f else 1f
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
          val melody = newMelody()
          viewModel.editingMelody = adapter.insert(melody)
          BeatClockPaletteConsumer.section?.melodies?.add(
            Section.MelodyReference(melody, 0.5f, Section.PlaybackType.Indefinite)
          )
        }
        setOnLongClickListener {
          newPatternMenu.show()
          true
        }
      }
    }
  }
}