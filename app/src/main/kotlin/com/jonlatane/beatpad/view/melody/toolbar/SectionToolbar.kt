package com.jonlatane.beatpad.view.melody.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.jonlatane.beatpad.MainApplication
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
class SectionToolbar(context: Context, viewModel: PaletteViewModel)
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

	private val meterButton: Button = button {
		text = "4\n4"
		setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
		backgroundResource = R.drawable.toolbar_melody_button
		padding = dip(0)
		typeface = MainApplication.chordTypefaceBold
//		singleLine = true
//		ellipsize = TextUtils.TruncateAt.MARQUEE
//		marqueeRepeatLimit = -1
//		isSelected = true
		onClick {
			context.toast("TODO!")
		}
	}.lparams {
		width = squareSize
		height = matchParent
		weight = 0f
	}

	private val keyButton: Button = button {
		text = "C Major"
		backgroundResource = R.drawable.toolbar_melody_button
		setPadding(dip(15), dip(10), dip(10), dip(10))
		gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
		onClick {
			context.toast("TODO!")
		}
		toolbarTextStyle()
	}.flexStyle()
	private val lengthButton: Button = button {
		text = "64/4\n4 bars"
		setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
		backgroundResource = R.drawable.toolbar_melody_button
		padding = dip(0)
		typeface = MainApplication.chordTypefaceBold
//		singleLine = true
//		ellipsize = TextUtils.TruncateAt.MARQUEE
//		marqueeRepeatLimit = -1
//		isSelected = true
		onClick {
			context.toast("TODO!")
		}
	}.longSquareButtonStyle().lparams { height = matchParent }

	private val editButton = imageButton {
		imageResource = R.drawable.edit_black
		backgroundResource = R.drawable.toolbar_melody_button
		padding = dip(10)
		scaleType = ImageView.ScaleType.FIT_CENTER
		onClick {
//			melodyViewModel.openedMelody?.transposeInPlace(1)
//			updateMelody()
			context.toast("TODO!")
		}
		onLongClick(returnValue = true) {
//			melodyViewModel.openedMelody?.transposeInPlace(12)
//			context.toast("Octave Up")
//			updateMelody()
			context.toast("TODO!")
		}
	}.squareButtonStyle()
}