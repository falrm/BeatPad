package com.jonlatane.beatpad.util

import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TypefaceSpan
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication


fun applyTypefaceToAll(view: View, typeface: Typeface): Unit = when (view) {
  is ViewGroup  -> {
    for (childIndex in 0 until view.childCount)
      applyTypefaceToAll(view.getChildAt(childIndex), typeface)
  }
  is TextView -> {
    view.typeface = typeface
    view.paintFlags = view.paintFlags or Paint.SUBPIXEL_TEXT_FLAG or Paint.DEV_KERN_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
  }
  else -> {}
}

fun MenuItem.applyTypeface(
  typeface: Typeface = MainApplication.chordTypefaceRegular
) {
  val newTitle = SpannableStringBuilder(title)
  newTitle.setSpan(
    object: TypefaceSpan(typeface) {},
    0,
    newTitle.length,
    Spannable.SPAN_INCLUSIVE_INCLUSIVE
  )
  title = newTitle
}

fun PopupMenu.applyTypeface(
  typeface: Typeface = MainApplication.chordTypefaceRegular
) {

  for (i in 0 until menu.size()) {
    val item = menu.getItem(i)
    item.applyTypeface(typeface)
  }
}