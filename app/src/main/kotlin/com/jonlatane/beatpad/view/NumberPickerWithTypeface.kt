package com.jonlatane.beatpad.view

import android.R
import android.content.Context
import android.widget.EditText
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.util.color
import org.jetbrains.anko.textColor


class NumberPickerWithTypeface @JvmOverloads constructor(
  context: Context,
  var typeface: Typeface = MainApplication.chordTypeface,
  var textSize: Float = 25f
) : android.widget.NumberPicker(context) {
  override fun addView(child: View) {
    super.addView(child)
    updateView(child)
  }

  override fun addView(child: View, index: Int,
              params: android.view.ViewGroup.LayoutParams) {
    super.addView(child, index, params)
    updateView(child)
  }

  override fun addView(child: View, params: android.view.ViewGroup.LayoutParams) {
    super.addView(child, params)
    updateView(child)
  }

  private fun updateView(view: View) {
    (view as? TextView)?.let {
      it.typeface = typeface
      it.textSize = textSize
      it.textColor = color(R.color.black)
    }
  }
}
