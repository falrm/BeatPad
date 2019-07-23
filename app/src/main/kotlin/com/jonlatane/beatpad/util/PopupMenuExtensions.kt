package com.jonlatane.beatpad.util

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.TypefaceSpan
import android.view.MenuItem.SHOW_AS_ACTION_ALWAYS
import android.view.View
import android.widget.PopupMenu
import com.jonlatane.beatpad.MainApplication
import android.widget.TextView
import android.view.ViewGroup


fun applyTypefaceToAll(view: View, typeface: Typeface): Unit = when (view) {
  is ViewGroup  -> {
    for (childIndex in 0 until view.childCount)
      applyTypefaceToAll(view.getChildAt(childIndex), typeface)
  }
  is TextView -> {
    view.setTypeface(typeface)
    view.paintFlags = view.paintFlags or Paint.SUBPIXEL_TEXT_FLAG or Paint.DEV_KERN_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
  }
  else -> {}
}

fun PopupMenu.applyTypeface(
  typeface: Typeface = MainApplication.chordTypefaceRegular,
  color: Int = Color.BLACK
)
//{
//  for (i in 0 until menu.size()) {
//    val item = menu.getItem(i)
//    item.setShowAsAction(SHOW_AS_ACTION_ALWAYS)
//    item.actionView = TextView(MainApplication.instance)
////    item.actionView?.let { applyTypefaceToAll(it, typeface) }
//
//  }
//}
 {

  for (i in 0 until menu.size()) {
    val item = menu.getItem(i)

    val newTitle = SpannableString(item.title)
    newTitle.setSpan(
      object: TypefaceSpan(typeface) {
        override fun updateDrawState(ds: TextPaint) {
          ds.color = color
          applyCustomTypeFace(ds, typeface)
        }

        override fun updateMeasureState(paint: TextPaint) {
          applyCustomTypeFace(paint, typeface)
        }

        fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
          val oldStyle: Int
          val old = paint.typeface
          oldStyle = if (old == null) { 0 } else { old.style }

          val fake = oldStyle and tf.style.inv()
          if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
          }

          if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
          }

          paint.typeface = tf
        }
      },
      0, newTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE
    )
    item.title = newTitle
  }
}