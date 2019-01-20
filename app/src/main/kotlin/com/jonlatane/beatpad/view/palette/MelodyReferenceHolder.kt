package com.jonlatane.beatpad.view.palette

import android.graphics.Color
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
import android.graphics.PorterDuff



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
  private val isMelodyReferenceEnabled: Boolean get()  = melodyReference != null && !melodyReference!!.isDisabled

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
    get() = melodyPosition >= viewModel.palette.parts.getOrNull(partPosition)?.melodies?.size ?: -1

	internal fun onPositionChanged() {
    if(isAddButton) {
      addMode()
    } else {
      editMode()
    }
	}

  internal fun animateMixOn() {
    layout.apply {
      name.isEnabled = false
      if(isMelodyReferenceEnabled) {
        volume.isEnabled = true
      }
      volume.animate().alpha(1f).start()
    }
  }

  internal fun animateMixOff() {
    layout.apply {
      name.isEnabled = true
      volume.animate().alpha(0f).withEndAction {
        volume.isEnabled = false
      }.start()
    }
  }

  internal fun enableMelodyReference() {
    if(melodyReference == null) {
      BeatClockPaletteConsumer.section?.melodies?.add(
        Section.MelodyReference(melody!!, 0.5f, Section.PlaybackType.Indefinite)
      )
    } else if(melodyReference!!.isDisabled) {
      melodyReference!!.playbackType = Section.PlaybackType.Indefinite
    }
  }

  internal fun disableMelodyReference() {
    melodyReference!!.playbackType = Section.PlaybackType.Disabled
    // Sanitization: Remove duplicates
    BeatClockPaletteConsumer.section?.melodies?.removeAll {
      it.melody == melody && it != melodyReference
    }
  }

	private fun editMode() {
		layout.apply {
      inclusion.apply {
        isEnabled = true
        imageResource = if(!isMelodyReferenceEnabled) {
          R.drawable.icons8_mute_100
        } else {
          R.drawable.icons8_speaker_100
        }
        alpha = 1f
        onClick {
          if(!isMelodyReferenceEnabled) {
            enableMelodyReference()
          } else {
            disableMelodyReference()
          }
          editMode()
        }
      }
      volume.apply {
        if(viewModel.editingMix) {
          isEnabled = isMelodyReferenceEnabled
          alpha = 1f
        } else {
          isEnabled = false
          alpha = 0f
        }
        progress = ((melodyReference?.volume ?: 0f) * 127).toInt()
        if(!isMelodyReferenceEnabled) {
          arrayOf(progressDrawable, thumb).forEach {
            it.colorFilter = null
          }
        } else {
          arrayOf(progressDrawable, thumb).forEach {
            it.setColorFilter( Color.WHITE, PorterDuff.Mode.SRC_IN)
          }
          onSeekBarChangeListener {
            onProgressChanged { _, progress, _ ->
              info("Setting melody volume to ${progress.toFloat() / 127f}")
              melodyReference?.volume = progress.toFloat() / 127f
            }
          }
        }
      }
			name.apply {
				text = ""
        isEnabled = !viewModel.editingMix
				backgroundResource = if(!isMelodyReferenceEnabled) {
          R.drawable.orbifold_chord
        } else {
          BeatClockPaletteConsumer.palette?.sections
            ?.indexOf(BeatClockPaletteConsumer.section)
            ?.let { sectionIndex ->
              SectionHolder.sectionColor(sectionIndex)
            } ?: R.drawable.orbifold_chord
        }
				setOnClickListener {
					viewModel.editingMelody = melody
				}
				setOnLongClickListener {
					editPatternMenu.show()
					true
				}
        alpha = if(!isMelodyReferenceEnabled) 0.5f else 1f
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
        alpha = 1f
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