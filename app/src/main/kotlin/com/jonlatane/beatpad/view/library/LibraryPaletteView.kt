package com.jonlatane.beatpad.view.library

import android.content.Context
import android.view.View
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView

class LibraryPaletteView(recyclerView: _RecyclerView): _RelativeLayout(recyclerView.context) {
  val title: TextView
  val id: TextView
  val modifiedDate: TextView
  init {
    title = textView {
      text = "Title"
      id = View.generateViewId()
    }.lparams(matchParent, wrapContent) {
      alignParentTop()
      alignParentLeft()
      alignParentRight()
    }
    id = textView {
      text = "OX-12346-asdf-asdf"
      id = View.generateViewId()
    }.lparams(matchParent, wrapContent) {
      alignParentLeft()
      below(title)
    }
    modifiedDate = textView {
      text = "5:43PM Jan 1 2019"
      id = View.generateViewId()
    }.lparams(matchParent, wrapContent) {
      rightOf(id)
      below(title)
      alignParentRight()
    }
  }
}