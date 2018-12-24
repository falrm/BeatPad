package com.jonlatane.beatpad.view.palette

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jonlatane.beatpad.R
import org.jetbrains.anko.*

class MelodyReferenceView(context: Context) : _RelativeLayout(context) {
  val name: TextView
  val inclusion: View? = null
  val volume: View? = null
  init {
    name = textView {
      textSize = 25f
      background = context.getDrawable(R.drawable.orbifold_chord)
      layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)
      minimumWidth = context.dip(90)
      isClickable = true
      isLongClickable = true
      gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
    }.lparams {
      width = matchParent
      height = wrapContent
    }
  }
}