package com.jonlatane.beatpad.view.melody.toolbar

import android.content.Context
import android.view.MotionEvent
import android.view.View
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.model.dsl.Patterns
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.view.HideableLinearLayout
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent

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
  fun <T: View> T.flexStyle() = this.lparams {
    width = matchParent
    height = dip(48)
    weight = 1f
  }


  fun <T: View> T.hidden() = this.lparams {
    width = dip(0)
    height = dip(48)
    weight = 0f
  }


  fun <T: View> T.squareButtonStyle() = this.lparams {
    width = dip(48)
    height = dip(48)
    weight = 0f
  }


  fun <T: View> T.longSquareButtonStyle() = this.lparams {
    width = dip(96)
    height = dip(48)
    weight = 0f
  }
}