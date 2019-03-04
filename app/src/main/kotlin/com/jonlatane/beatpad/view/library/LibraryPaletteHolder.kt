package com.jonlatane.beatpad.view.library

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView

class LibraryPaletteHolder(
  viewModel: LibraryViewModel,
  recyclerView: _RecyclerView
): RecyclerView.ViewHolder(
  recyclerView.run { libraryPaletteView().lparams(matchParent, wrapContent) }
) {
  val view get() = itemView as LibraryPaletteView
}