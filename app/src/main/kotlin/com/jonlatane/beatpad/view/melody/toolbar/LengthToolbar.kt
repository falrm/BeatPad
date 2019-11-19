package com.jonlatane.beatpad.view.melody.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.widget.*
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.HideAnimation
import com.jonlatane.beatpad.util.vibrate
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onFocusChange
import org.jetbrains.anko.sdk25.coroutines.onTouch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

@SuppressLint("ViewConstructor")
class LengthToolbar(context: Context, viewModel: PaletteViewModel)
  : Toolbar(context, viewModel), AnkoLogger {
  private val subdivisionsEditTextEditText: EditText
  private val perBeatEditText: EditText
  private val totalEditText: EditText
  private val subdivisionsText: TextView
  private val perBeatText: TextView
  private val totalBeatsOrBarsText: TextView

  private fun EditText.makeDraggableNumber(minValue: Int, maxValue: Int) {
    val startPoint = PointF()
    var startValue = 0
    onTouch { v, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        startPoint.x = event.x
        startPoint.y = event.y
        startValue = text.toString().toInt()
        info("Set start value to $startValue for (${startPoint.x},${startPoint.y}")
      }
      val xIncrement = 30
      val yIncrement = 30
      if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_OUTSIDE) {
        if(abs(event.x - startPoint.x) > xIncrement || abs(event.y - startPoint.y) > yIncrement) {
          val currentValue = text.toString().toInt()
          var updatedValue = round(
            startValue + (event.x - startPoint.x) / xIncrement - round((event.y - startPoint.y) / yIncrement)
          ).toInt()
          updatedValue = min(updatedValue, maxValue)
          updatedValue = max(updatedValue, minValue)
          info("Updated value is $updatedValue for (${event.x},${event.y})")
          if(currentValue != updatedValue) {
            text.clear()
            text.append(updatedValue.toString())
            vibrate(10)
          }
        }
      }
    }
  }
  init {
    subdivisionsEditTextEditText = editText {
      inputType = InputType.TYPE_CLASS_NUMBER
      gravity = Gravity.CENTER
      typeface = MainApplication.chordTypefaceBold
//        toolbarButtonEditTextStyle()
      text.append("64")
      backgroundResource = R.drawable.toolbar_melody_button
      makeDraggableNumber(1, 9999)
//      onFocusChange { v, hasFocus ->
//        if(!hasFocus) {
//
//        }
//      }
    }.mediumSquareButtonStyle()
    subdivisionsText = textView("subdivisions") {
      toolbarTextStyle()
      gravity = Gravity.CENTER
    }.flexStyle()

    perBeatEditText = editText {
      inputType = InputType.TYPE_CLASS_NUMBER
      gravity = Gravity.CENTER
      typeface = MainApplication.chordTypefaceBold
//        toolbarButtonEditTextStyle()
      text.append("4")
      backgroundResource = R.drawable.toolbar_melody_button
      makeDraggableNumber(1, 24)
    }.squareButtonStyle()
    perBeatText = textView("per beat") {
      toolbarTextStyle()
      gravity = Gravity.CENTER
    }.flexStyle()

    totalEditText = editText {
      inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
      gravity = Gravity.CENTER
      typeface = MainApplication.chordTypefaceBold
//        toolbarButtonEditTextStyle()
      text.append("16")
      backgroundResource = R.drawable.toolbar_melody_button

      onFocusChange { v, hasFocus ->
        if(!hasFocus) {

        }
      }
    }.longSquareButtonStyle()
    totalBeatsOrBarsText = textView("beats") {
      toolbarTextStyle()
      gravity = Gravity.CENTER
    }.flexStyle()
    val closeButton = imageButton {
      imageResource = R.drawable.notehead_x
      onClick {
        this@LengthToolbar.hide()
        viewModel.melodyViewModel.melodyEditingToolbar.lengthButtonFrame.show(animation = HideAnimation.HORIZONTAL_ALPHA)
        viewModel.melodyViewModel.sectionToolbar.lengthButtonFrame.show(animation = HideAnimation.HORIZONTAL_ALPHA)
      }
    }.squareButtonStyle()
  }
}