package com.jonlatane.beatpad.view.palette

import BeatClockPaletteConsumer
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.widget.PopupMenu
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.storage.Storage
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener
import java.net.URI


class MelodyReferenceHolder(
  val viewModel: PaletteViewModel,
  val adapter: MelodyReferenceAdapter,
  val layout: MelodyReferenceView = adapter.recyclerView.run {
    MelodyReferenceView(context).lparams {
			width = matchParent
      height = wrapContent
		}
	}
) : RecyclerView.ViewHolder(layout), AnkoLogger, Storage {
  override val storageContext: Context get() = context
  private val partPosition: Int get() = adapter.partPosition
	val part: Part? get() = adapter.part
	internal val melody: Melody<*>? get() = part?.melodies?.getOrNull(adapterPosition)
  private val melodyReference: Section.MelodyReference?
    get() = BeatClockPaletteConsumer.section?.melodies?.firstOrNull { it.melody == melody }
  private val context get() = adapter.recyclerView.context

	private val newMelodyMenu = PopupMenu(layout.context, layout)
	internal val editPatternMenu = PopupMenu(layout.context, layout)
  private val isMelodyReferenceEnabled: Boolean get()  = melodyReference != null && !melodyReference!!.isDisabled
  private val pasteMelody get() = newMelodyMenu.menu.findItem(R.id.pasteMelody)

	init {
		newMelodyMenu.inflate(R.menu.part_melody_new_menu)
		newMelodyMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.composeMidiMelody -> adapter.createAndOpenDrawnMelody()
				R.id.recordMidiMelody  -> context.toast("TODO!")
        R.id.pasteMelody -> {
          getClipboardMelody()?.let { adapter.createAndOpenDrawnMelody(it) }
            ?: context.toast("Failed to read Melody from clipboard.")
        }
				else                   -> context.toast("Impossible!")
			}
			true
		}
		editPatternMenu.inflate(R.menu.part_melody_edit_menu)
		editPatternMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.editMelody   -> viewModel.editingMelody = melody
				R.id.removeMelody -> showConfirmDialog(
					context,
					promptText = "Really delete this melody?",
					yesText = "Yes, delete melody"
				) {
					val melody = part!!.melodies.removeAt(this@MelodyReferenceHolder.adapterPosition)
          viewModel.palette.sections.forEach { section ->
            val removedReferences = section.melodies.filter { it.melody == melody }
            section.melodies.removeAll(removedReferences)
          }
					adapter.notifyItemRemoved(this@MelodyReferenceHolder.adapterPosition)
//					adapter.notifyItemRangeChanged(
//            adapterPosition,
//            part!!.melodies.size - adapterPosition
//					)
				}
        R.id.copyMelody   -> copyMelody()
				else              -> context.toast("TODO!")
			}
			true
		}
	}

  val isAddButton: Boolean
    get() = adapterPosition >= viewModel.palette.parts.getOrNull(partPosition)?.melodies?.size ?: -1

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

  internal fun enableMelodyReference() = viewModel.melodyViewModel.enableMelodyReference(melody!!, melodyReference)

  internal fun disableMelodyReference() = viewModel.melodyViewModel.disableMelodyReference(melody!!, melodyReference!!)

	private fun editMode() {
		layout.apply {
      inclusion.apply {
        isEnabled = true
        imageResource = when {
          !isMelodyReferenceEnabled                                        -> R.drawable.repeat_off
          melodyReference?.playbackType is Section.PlaybackType.Indefinite -> R.drawable.repeat
          else                                                             -> R.drawable.repeat_one
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
          BeatClockPaletteConsumer.currentSectionDrawable
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
          adapter.createAndOpenDrawnMelody()
        }
        setOnLongClickListener {
          showNewMelodyMenu()
          true
        }
      }
    }
  }

  private fun showNewMelodyMenu() {
    pasteMelody.isEnabled = getClipboardMelody() != null
    newMelodyMenu.show()
  }

  private fun getClipboardMelody(): Melody<*>? = try {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.primaryClip?.getItemAt(0)?.text?.let { URI(it.toString()) }
      ?.let { uri ->
        uri.toEntity("melody", "v1", Melody::class)
      }
  } catch(t: Throwable) {
    error("Failed to deserialize melody", t)
    null
  }

  fun copyMelody() {
    val text = melody?.toURI()?.toString() ?: ""
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("BeatScratch Melody", text)
    clipboard.setPrimaryClip(clip)
    context.toast("Copied BeatScratch Melody data to clipboard!")
  }
}