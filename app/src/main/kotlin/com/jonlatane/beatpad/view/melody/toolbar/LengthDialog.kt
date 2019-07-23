package com.jonlatane.beatpad.view.melody.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.util.applyTypefaceToAll
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.numberPickerWithTypeface
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onValueChanged

class LengthDialog(context: Context, val melodyViewModel: MelodyViewModel) {
  val melody: Melody<*>? get() = melodyViewModel.openedMelody

  private lateinit var lengthPicker: NumberPicker
  lateinit var subdivisionsPerBeatPicker: NumberPicker
  private lateinit var beatCount: TextView
  private lateinit var beatsText: TextView

  fun updateText() {
    melody?.apply {
      lengthPicker.value = length
      subdivisionsPerBeatPicker.value = subdivisionsPerBeat
      updateBeatCount(subdivisionsPerBeat, length)
    }
  }

  fun show() {
    // Needed to prevent error on call to show()
    (baseLayout.parent as? ViewGroup)?.removeView(baseLayout)
    alert.show()
  }
  private lateinit var baseLayout: RelativeLayout
  private val alert = context.alert {
    customView {
      baseLayout = relativeLayout {
        val title  = textView {
          id = View.generateViewId()
          typeface = MainApplication.chordTypefaceBold
          text = "Melody Length"
          textSize = 18f
        }.lparams(wrapContent, wrapContent) {
          centerHorizontally()
        }
        val numberPickers = linearLayout {
          orientation = LinearLayout.HORIZONTAL
          id = View.generateViewId()

          lengthPicker = numberPickerWithTypeface {
            value = 1
            minValue = 1
            maxValue = 20000
            wrapSelectorWheel = false
            onValueChanged { _, _, _ ->
              applyChange()
            }
          }.lparams(wrapContent, wrapContent)

          textView {
            text = "subdivisions"
            gravity = Gravity.CENTER
            typeface = MainApplication.chordTypeface
          }.lparams(wrapContent, matchParent)

          textView {
            text = "/"
            gravity = Gravity.CENTER
            textSize *= 1.5f
            typeface = MainApplication.chordTypefaceBold
          }.lparams(wrapContent, matchParent)

          subdivisionsPerBeatPicker = numberPickerWithTypeface {
            value = 1
            minValue = 1
            //this.displayedValues = arrayOf("2")
            maxValue = 24
            wrapSelectorWheel = false
            onValueChanged { _, _, _ ->
              applyChange()
            }
          }.lparams(wrapContent, wrapContent)
          textView {
            text = "per beat"
            gravity = Gravity.CENTER
            typeface = MainApplication.chordTypeface
          }.lparams(wrapContent, matchParent)
        }.lparams(wrapContent, wrapContent) {
          centerHorizontally()
          below(title)
        }
        linearLayout {
          id = View.generateViewId()
          beatCount = textView {
            text = "?"
            gravity = Gravity.CENTER
            textSize *= 1.5f
            typeface = MainApplication.chordTypefaceBold
          }.lparams(wrapContent, matchParent)

          beatsText = textView {
            text = "beats"
            gravity = Gravity.CENTER
            typeface = MainApplication.chordTypeface
          }.lparams(wrapContent, matchParent)
        }.lparams(wrapContent, wrapContent) {
          marginStart = dip(10)
          centerHorizontally()
          below(numberPickers)
        }
      }//.lparams(wrapContent, wrapContent)
    }
  }

  @SuppressLint("SetTextI18n")
  fun applyChange() {
    val targetSubdivisionsPerBeat = subdivisionsPerBeatPicker.value
    val targetLength = lengthPicker.value
    melody?.apply {
      if(length != targetLength || subdivisionsPerBeat != targetSubdivisionsPerBeat) {
        length = targetLength
        subdivisionsPerBeat = targetSubdivisionsPerBeat
        melodyViewModel.updateToolbarsAndMelody()
        //updateBeatCount(targetSubdivisionsPerBeat, targetLength)
      }
    }
  }

  private fun updateBeatCount(subdivisionsPerBeat: Int, length: Int) {
    beatCount.text = "%.3f"
      .format(length.toFloat() / subdivisionsPerBeat)
      .trim('0')
      .trimEnd('.')
      .also {
        beatsText.text = if(it == "1") "beat" else "beats"
      }
  }
}