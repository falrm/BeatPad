package com.jonlatane.beatpad.view.melody.toolbar

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
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onValueChanged

class LengthDialog(context: Context, val melodyViewModel: MelodyViewModel) {
  val melody: Melody<*>? get() = melodyViewModel.openedMelody

  private lateinit var lengthPicker: NumberPicker
  lateinit var subdivisionsPerBeatPicker: NumberPicker
  private lateinit var beatCount: TextView

  fun updateText() {
    lengthPicker.value = melody?.length ?: 1
    subdivisionsPerBeatPicker.value = melody?.subdivisionsPerBeat ?: 1
  }

  fun show() {
    (lengthLayout.parent as? ViewGroup)?.removeView(lengthLayout)
    alert.show()
  }
  private lateinit var lengthLayout: RelativeLayout
  private val alert = context.alert {
    customView {
      lengthLayout = relativeLayout {
        val numberPickers = linearLayout {
          orientation = LinearLayout.HORIZONTAL
          id = View.generateViewId()

          lengthPicker = numberPicker {
            value = 1
            minValue = 1
            maxValue = 20000
            wrapSelectorWheel = false
            onValueChanged { picker, oldVal, newVal ->

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

          subdivisionsPerBeatPicker = numberPicker {
            value = 1
            minValue = 1
            //this.displayedValues = arrayOf("2")
            maxValue = 24
            wrapSelectorWheel = false
          }.lparams(wrapContent, wrapContent)
          textView {
            text = "per beat"
            gravity = Gravity.CENTER
            typeface = MainApplication.chordTypeface
          }.lparams(wrapContent, matchParent)
        }.lparams(wrapContent, wrapContent) {
          centerHorizontally()
        }
        val beatTotal = linearLayout {
          id = View.generateViewId()
          beatCount = textView {
            text = "?"
            gravity = Gravity.CENTER
            textSize *= 1.5f
            typeface = MainApplication.chordTypefaceBold
          }.lparams(wrapContent, matchParent)

          textView {
            text = "beats"
            gravity = Gravity.CENTER
            typeface = MainApplication.chordTypeface
          }.lparams(wrapContent, matchParent)
        }.lparams(wrapContent, wrapContent) {
          centerHorizontally()
          below(numberPickers)
        }
        button {
          text = "OK"
          id = View.generateViewId()
          typeface = MainApplication.chordTypefaceBold
          onClick {
            this@button.requestFocus()
            val targetSubdivisionsPerBeat = subdivisionsPerBeatPicker.value
            val targetLength = lengthPicker.value
            melody?.apply {
              if(length != targetLength || subdivisionsPerBeat != targetSubdivisionsPerBeat) {
                length = targetLength
                subdivisionsPerBeat = targetSubdivisionsPerBeat
                melodyViewModel.updateToolbarsAndMelody()
              }
            }
          }
        }.lparams(wrapContent, wrapContent) {
          below(beatTotal)
          centerHorizontally()
        }
      }//.lparams(wrapContent, wrapContent)
    }
  }
}