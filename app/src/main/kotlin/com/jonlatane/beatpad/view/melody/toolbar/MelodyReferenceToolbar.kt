package com.jonlatane.beatpad.view.melody.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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
		backgroundResource = R.drawable.toolbar_melody_button
		padding = dip(12)
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
		backgroundResource = R.drawable.toolbar_melody_button
		padding = dip(10)
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
		backgroundResource = R.drawable.toolbar_melody_button
		padding = dip(7)
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

	var editModeActive: Boolean = false
	set(value) {
		val changed = field != value
		field = value
		editButton.apply {
			backgroundResource = if(value) R.drawable.toolbar_melody_button_active
				else R.drawable.toolbar_melody_button
			padding = dip(10)
		}
		if (value) {
			melodyViewModel.melodyEditingToolbar.show()
			melodyViewModel.melodyEditingToolbar.lengthButtonFrame.show(animation = HideAnimation.HORIZONTAL_THEN_VERTICAL)
			melodyViewModel.melodyEditingModifiers.show()
			melodyViewModel.layoutType = MelodyViewModel.LayoutType.LINEAR
//				viewModel.backStack.push {
//					if (!melodyViewModel.melodyEditingToolbar.isHidden) {
//						melodyViewModel.melodyEditingToolbar.hide()
//						melodyViewModel.melodyEditingModifiers.hide()
//						true
//					} else false
//				}
		} else {
			melodyViewModel.melodyEditingToolbar.hide()
			melodyViewModel.lengthToolbar.hide()
			melodyViewModel.melodyEditingModifiers.hide()
			melodyViewModel.layoutType = MelodyViewModel.LayoutType.GRID
		}
	}
	private val editButton = imageButton {
		imageResource = R.drawable.edit_black
		backgroundResource = R.drawable.toolbar_melody_button
		padding = dip(10)
		scaleType = ImageView.ScaleType.FIT_CENTER
		onClick {
			editModeActive = !editModeActive
		}
		onLongClick(returnValue = true) {
			//			melodyViewModel.openedMelody?.transposeInPlace(-12)
//			storageContext.toast("Octave Down")
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
//			storageContext.toast("Octave Up")
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
//			storageContext.toast("Octave Down")
//			updateMelody()
//		}
//	}.squareButtonStyle()

	@SuppressLint("SetTextI18n")
	fun updateButtonText() {
		volume.apply {
			isEnabled = melodyViewModel.isMelodyReferenceEnabled
			progress = ((melodyReference?.volume ?: 0f) * 127).toInt()
			if(melodyViewModel.isMelodyReferenceEnabled) {
				thumbColorFilterColor = BeatClockPaletteConsumer.currentSectionColor
				onSeekBarChangeListener {
					onProgressChanged { _, progress, _ ->
						melodyReference?.volume = progress.toFloat() / 127f
					}
				}
			} else {
				thumbColorFilterColor = Color.WHITE
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