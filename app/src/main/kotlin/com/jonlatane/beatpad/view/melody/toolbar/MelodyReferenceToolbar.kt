package com.jonlatane.beatpad.view.melody.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.util.*
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener

@SuppressLint("ViewConstructor")
class MelodyReferenceToolbar(context: Context, viewModel: PaletteViewModel)
	: Toolbar(context, viewModel), AnkoLogger
{
	val displayTypeButton = imageButton {
		imageResource = R.drawable.notehead_filled
		scaleType = ImageView.ScaleType.FIT_CENTER
		onClick {
			melodyViewModel.displayType = when(melodyViewModel.displayType) {
				MelodyViewModel.DisplayType.COLORBLOCK -> MelodyViewModel.DisplayType.NOTATION
				else                                   -> MelodyViewModel.DisplayType.COLORBLOCK
			}
		}
	}.squareButtonStyle()
	val layoutTypeButton = imageButton {
		imageResource = R.drawable.line
		scaleType = ImageView.ScaleType.FIT_CENTER
		onClick {
			melodyViewModel.layoutType = when(melodyViewModel.layoutType) {
				MelodyViewModel.LayoutType.LINEAR -> MelodyViewModel.LayoutType.GRID
				else                              -> MelodyViewModel.LayoutType.LINEAR
			}
		}
	}.squareButtonStyle()

	private val volumeButton = imageButton {
		imageResource = R.drawable.repeat
		onClick {
			with(viewModel.melodyViewModel) {
				if(!isMelodyReferenceEnabled) {
					enableMelodyReference(melody!!, melodyReference)
				} else {
					disableMelodyReference(melody!!, melodyReference!!)
				}
			}
		}
	}.squareButtonStyle()

	val volume = seekBar {
		id = View.generateViewId()
		max = 127
		val padding = dip(5)
		setPadding(padding,padding,padding,padding)
	}.flexStyle()

	private val editButton = imageButton {
		imageResource = R.drawable.edit_black
		scaleType = ImageView.ScaleType.FIT_CENTER
		onClick {
			if (melodyViewModel.melodyEditingToolbar.isHidden) {
				melodyViewModel.melodyEditingToolbar.show()
				melodyViewModel.melodyEditingModifiers.show()
				viewModel.backStack.push {
					if (!melodyViewModel.melodyEditingToolbar.isHidden) {
						melodyViewModel.melodyEditingToolbar.hide()
						melodyViewModel.melodyEditingModifiers.hide()
						true
					} else false
				}
			} else {
				melodyViewModel.melodyEditingToolbar.hide()
				melodyViewModel.melodyEditingModifiers.hide()
			}
			//			melodyViewModel.openedMelody?.transposeInPlace(-1)
//			updateMelody()
		}
		onLongClick(returnValue = true) {
			//			melodyViewModel.openedMelody?.transposeInPlace(-12)
//			context.toast("Octave Down")
//			updateMelody()
		}
	}.squareButtonStyle()


//	private val upButton = imageButton {
//		imageResource = R.drawable.icons8_sort_up_100
//		scaleType = ImageView.ScaleType.FIT_CENTER
//		onClick {
//      melodyViewModel.openedMelody?.transposeInPlace(1)
//			updateMelody()
//		}
//		onLongClick(returnValue = true) {
//      melodyViewModel.openedMelody?.transposeInPlace(12)
//			context.toast("Octave Up")
//			updateMelody()
//		}
//	}.squareButtonStyle()
//
//	private val downButton = imageButton {
//		imageResource = R.drawable.icons8_sort_down_100
//		scaleType = ImageView.ScaleType.FIT_CENTER
//		onClick {
//			melodyViewModel.openedMelody?.transposeInPlace(-1)
//			updateMelody()
//		}
//		onLongClick(returnValue = true) {
//			melodyViewModel.openedMelody?.transposeInPlace(-12)
//			context.toast("Octave Down")
//			updateMelody()
//		}
//	}.squareButtonStyle()

	@SuppressLint("SetTextI18n")
	fun updateButtonText() {
		volume.apply {
			isEnabled = melodyViewModel.isMelodyReferenceEnabled
			progress = ((melodyReference?.volume ?: 0f) * 127).toInt()
			if(melodyViewModel.isMelodyReferenceEnabled) {
				arrayOf(progressDrawable, thumb).forEach {
					it.setColorFilter(BeatClockPaletteConsumer.currentSectionColor, PorterDuff.Mode.SRC_IN)
				}
				onSeekBarChangeListener {
					onProgressChanged { _, progress, _ ->
						info("Setting melody volume to ${progress.toFloat() / 127f}")
						melodyReference?.volume = progress.toFloat() / 127f
					}
				}
			} else {
				arrayOf(progressDrawable, thumb).forEach {
					it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
				}
			}
		}


		context.color(R.color.colorPrimaryDark)
		volumeButton.imageResource = when {
			!melodyViewModel.isMelodyReferenceEnabled                        -> R.drawable.repeat_off
			melodyReference?.playbackType is Section.PlaybackType.Indefinite -> R.drawable.repeat
			else                                                             -> R.drawable.repeat_one
		}
	}
	private fun updateMelody() = viewModel.melodyBeatAdapter.notifyDataSetChanged()
}