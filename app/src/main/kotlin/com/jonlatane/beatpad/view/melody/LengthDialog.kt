package com.jonlatane.beatpad.view.melody

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.RelativeLayout
import com.jonlatane.beatpad.model.Melody
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class LengthDialog(context: Context) {
  var melody: Melody<*>? = null
    set(value) {
      field = value
      lengthPicker.value = value?.length ?: 1
      subdivisionsPerBeatPicker.value = value?.subdivisionsPerBeat ?: 1
    }

  private lateinit var lengthPicker: NumberPicker
  lateinit var subdivisionsPerBeatPicker: NumberPicker

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
          }.lparams(wrapContent, wrapContent)

          textView {
            text = "/"
            gravity = Gravity.CENTER
          }.lparams(wrapContent, matchParent)

          subdivisionsPerBeatPicker = numberPicker {
            value = 1
            minValue = 1
            this.displayedValues = arrayOf("2")
            maxValue = 24
            wrapSelectorWheel = false
          }.lparams(wrapContent, wrapContent)
        }.lparams(wrapContent, wrapContent) {
          centerHorizontally()
        }
        button {
          text = "OK"
          id = View.generateViewId()
          onClick {

          }
        }.lparams(wrapContent, wrapContent) {
          below(numberPickers)
          centerHorizontally()
        }
      }//.lparams(wrapContent, wrapContent)
    }
  }
}