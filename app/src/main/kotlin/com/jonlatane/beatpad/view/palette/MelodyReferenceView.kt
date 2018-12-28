package com.jonlatane.beatpad.view.palette

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.jonlatane.beatpad.R
import org.jetbrains.anko.*

class MelodyReferenceView(context: Context) : _RelativeLayout(context) {
  val name: TextView
  val inclusion: ImageButton
  val volume: SeekBar
  init {
    clipChildren = false
    clipToPadding = false
    name = textView {
      id = View.generateViewId()
      textSize = 25f
      background = context.getDrawable(R.drawable.orbifold_chord)
      layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)
      minimumWidth = dip(90)
      isClickable = true
      isLongClickable = true
      gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
    }.lparams(matchParent, dip(40))

    inclusion = imageButton {
      id = View.generateViewId()
      scaleType = ImageView.ScaleType.FIT_CENTER
      alpha = 1f
    }.lparams(dip(40), dip(40)) {
      alignParentLeft()
      alignParentBottom()
      alignParentTop()
    }

    volume = seekBar {
      id = View.generateViewId()
      alpha = 1f
      max = 127
      val padding = dip(5)
      setPadding(padding,padding,padding,padding)
    }.lparams(matchParent, dip(40)) {
      rightOf(inclusion)
      alignParentBottom()
      alignParentTop()
      alignParentRight()
    }
  }
}