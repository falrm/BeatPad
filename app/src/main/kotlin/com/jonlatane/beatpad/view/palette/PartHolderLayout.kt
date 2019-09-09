package com.jonlatane.beatpad.view.palette

import android.support.constraint.ConstraintSet
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.Gravity
import android.widget.SeekBar
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.recyclerview.v7.recyclerView

interface PartHolderLayout {
  fun TextView.partNamePadding() {
    horizontalPadding = dip(5)
    verticalPadding = dip(5)
  }
}