package com.jonlatane.beatpad.view.melody

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.RelativeLayout
import org.jetbrains.anko.*

class LengthDialog(context: Context) {
  fun show() {
    (lengthLayout.parent as? ViewGroup)?.removeView(lengthLayout)
    alert.show()
  }

  internal lateinit var lengthPicker: NumberPicker private set
  internal lateinit var subdivisionsPerBeatPicker: NumberPicker private set
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
          }.lparams(wrapContent, wrapContent)

          textView {
            text = "/"
            gravity = Gravity.CENTER
          }.lparams(wrapContent, matchParent)

          subdivisionsPerBeatPicker = numberPicker {
            value = 1
            minValue = 1
            maxValue = 24
            wrapSelectorWheel = false
          }.lparams(wrapContent, wrapContent)
        }.lparams(wrapContent, wrapContent) {
          centerHorizontally()
        }
        button {
          text = "OK"
          id = View.generateViewId()
        }.lparams(wrapContent, wrapContent) {
          below(numberPickers)
          centerHorizontally()
        }
      }//.lparams(wrapContent, wrapContent)
    }
  }
}