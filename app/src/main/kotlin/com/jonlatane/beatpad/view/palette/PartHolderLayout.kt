package com.jonlatane.beatpad.view.palette

import android.widget.TextView
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.verticalPadding

interface PartHolderLayout {
  fun TextView.partNamePadding() {
    horizontalPadding = dip(5)
    verticalPadding = dip(5)
  }
}