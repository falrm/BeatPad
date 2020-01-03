package com.jonlatane.beatpad.view.melody.toolbar

import android.content.Context
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.model.dsl.Patterns
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.tablet
import com.jonlatane.beatpad.view.HideableLinearLayout
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*
import kotlin.math.round

abstract class Toolbar(context: Context, val viewModel: PaletteViewModel)
  : HideableLinearLayout(context), Patterns
{
  init {
    orientation = HORIZONTAL
    backgroundColor = context.color(R.color.colorPrimaryDark)
  }
  val melodyViewModel get() = viewModel.melodyViewModel
  val melody get() = melodyViewModel.openedMelody
  val melodyReference get() = melodyViewModel.melodyReference
  fun <T: View> T.flexStyle(extensions: LayoutParams.() -> Unit = {}) = this.lparams(matchParent, squareSize) {
    weight = 1f
    extensions()
  }


  val squareSize: Int = context.configuration.run {
    when {
      portrait -> dip(48)
      else -> when {
        tablet -> dip(48)
        else -> dip(40)
      }
    }
  }
  fun <T: View> T.hidden() = this.lparams {
    width = dip(0)
    height = squareSize
    weight = 0f
  }


  fun <T: View> T.squareButtonStyle() = this.lparams {
    width = squareSize
    height = squareSize
    weight = 0f
  }


  fun <T: View> T.mediumSquareButtonStyle() = this.lparams {
    width = round(1.5 * squareSize).toInt()
    height = squareSize
    weight = 0f
  }


  fun <T: View> T.longSquareButtonStyle() = this.lparams {
    width = 2 * squareSize
    height = squareSize
    weight = 0f
  }

  fun TextView.toolbarButtonTextStyle() {
    singleLine = true
    ellipsize = TextUtils.TruncateAt.MARQUEE
    marqueeRepeatLimit = -1
    isSelected = true
    allCaps = true
    typeface = MainApplication.chordTypefaceBold
  }
  fun TextView.toolbarButtonEditTextStyle() {
    singleLine = true
    allCaps = true
    typeface = MainApplication.chordTypefaceBold
  }
  fun TextView.toolbarTextStyle() {
    singleLine = true
    ellipsize = TextUtils.TruncateAt.MARQUEE
    marqueeRepeatLimit = -1
    isSelected = true
    textColor = color(R.color.white)
    textScaleX = 0.9f
    typeface = MainApplication.chordTypeface
  }
}