package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import org.jetbrains.anko.dip
import org.jetbrains.anko.wrapContent

class SectionHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
  TextView(parent.context).apply {
    textSize = 25f
    background = context.getDrawable(R.drawable.orbifold_chord)
    layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)
    minimumWidth = context.dip(90)
    isClickable = true
    isLongClickable = true
    gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
    typeface = MainApplication.chordTypeface
    setPadding(
      dip(50),
      dip(5),
      dip(50),
      dip(5)
    )
  }
) {
  val textView = itemView as TextView
}