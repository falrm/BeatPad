package com.jonlatane.beatpad.view.palette

import android.content.Context
import android.support.constraint.ConstraintSet
import android.text.TextUtils
import android.view.Gravity
import android.widget.SeekBar
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.view.HideableRecyclerView
import com.jonlatane.beatpad.view.hideableRecyclerView
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet

class PartHolderView(ctx: Context): _ConstraintLayout(ctx), PartHolderLayout {
  val partName: TextView = textView {
    id = R.id.part_name
    textSize = 20f
    singleLine = true
    ellipsize = TextUtils.TruncateAt.MARQUEE
    marqueeRepeatLimit = -1
    isSelected = true
    typeface = MainApplication.chordTypeface
    textScaleX = 0.9f
    gravity = Gravity.CENTER_HORIZONTAL
    backgroundResource = R.drawable.part_background
    elevation = 2f
    //partNamePadding()
  }.lparams(0, wrapContent)

  val melodyReferenceRecycler: HideableRecyclerView = hideableRecyclerView {
    id = R.id.part_patterns
    elevation = 1f
    setHasFixedSize(true)
  }.lparams(0, 0)

  val volumeSlider: SeekBar = seekBar {
    id = R.id.part_volume
    elevation = 2f
    alpha = 0f
    max = 127

  }.lparams(0, 0)

  init {
    applyConstraintSet {
      partName {
        connect(
          ConstraintSetBuilder.Side.TOP to ConstraintSetBuilder.Side.TOP of ConstraintSet.PARENT_ID,
          ConstraintSetBuilder.Side.START to ConstraintSetBuilder.Side.START of ConstraintSet.PARENT_ID,
          ConstraintSetBuilder.Side.END to ConstraintSetBuilder.Side.END of ConstraintSet.PARENT_ID
        )
      }
      melodyReferenceRecycler {
        connect(
          ConstraintSetBuilder.Side.TOP to ConstraintSetBuilder.Side.BOTTOM of R.id.part_name,
          ConstraintSetBuilder.Side.BOTTOM to ConstraintSetBuilder.Side.BOTTOM of ConstraintSet.PARENT_ID,
          ConstraintSetBuilder.Side.START to ConstraintSetBuilder.Side.START of ConstraintSet.PARENT_ID,
          ConstraintSetBuilder.Side.END to ConstraintSetBuilder.Side.END of ConstraintSet.PARENT_ID
        )
      }
      volumeSlider {
        connect(
          ConstraintSetBuilder.Side.TOP to ConstraintSetBuilder.Side.TOP of R.id.part_name,
          ConstraintSetBuilder.Side.BOTTOM to ConstraintSetBuilder.Side.BOTTOM of R.id.part_name,
          ConstraintSetBuilder.Side.START to ConstraintSetBuilder.Side.START of R.id.part_name,
          ConstraintSetBuilder.Side.END to ConstraintSetBuilder.Side.END of R.id.part_name
        )
      }
    }
  }
}