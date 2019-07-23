package com.jonlatane.beatpad.view

import android.R
import android.content.Context
import android.widget.EditText
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.util.applyTypefaceToAll
import com.jonlatane.beatpad.util.color
import org.jetbrains.anko.textColor


class NumberPickerWithTypeface @JvmOverloads constructor(
  context: Context
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
    { MainApplication.chordTypefaceRegular }()?.let { applyTypefaceToAll(view, it) }
  }
}
