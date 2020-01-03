package com.jonlatane.beatpad.view.palette

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.HideableView
import com.jonlatane.beatpad.util.InstaRecycler
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.util.smartrecycler.updateSmartHolders
import com.jonlatane.beatpad.view.colorboard.ColorboardConfiguration
import com.jonlatane.beatpad.view.keyboard.KeyboardConfiguration
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.melody.renderer.BaseMelodyBeatRenderer.ViewType
import com.jonlatane.beatpad.view.melody.toolbar.Toolbar
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import com.jonlatane.beatpad.view.orbifold.OrbifoldConfiguration
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick


class ViewModeToolbar(
  override val configurationContext: Context,
  viewModel: PaletteViewModel
) : Toolbar(configurationContext, viewModel), AnkoLogger, OrbifoldConfiguration, ColorboardConfiguration, KeyboardConfiguration, Storage, HideableView {
  override val storageContext: Context get() = configurationContext
  private val metronomeImage = context.resources.getDrawable(R.drawable.noun_metronome_415494_000000, null).apply {
    setBounds(0, 0, 60, 60)
  }

  init {
    orientation = LinearLayout.HORIZONTAL
    backgroundColor = context.color(R.color.colorPrimaryDark)
  }

  fun <T: View> T.palletteToolbarStyle(): T = this.lparams(matchParent, matchParent) {
    weight = 1f
  }

  val displayTypeButton = imageButton {
    imageResource = R.drawable.notehead_filled
    backgroundResource = R.drawable.toolbar_button
    padding = dip(12)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      melodyViewModel.displayType = when(melodyViewModel.displayType) {
        MelodyViewModel.DisplayType.COLORBLOCK -> MelodyViewModel.DisplayType.NOTATION
        else                                   -> MelodyViewModel.DisplayType.COLORBLOCK
      }
    }
  }.palletteToolbarStyle()
  val layoutTypeButton = imageButton {
    imageResource = R.drawable.line
    backgroundResource = R.drawable.toolbar_button
    padding = dip(10)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      melodyViewModel.layoutType = when(melodyViewModel.layoutType) {
        MelodyViewModel.LayoutType.LINEAR -> MelodyViewModel.LayoutType.GRID
        else                              -> MelodyViewModel.LayoutType.LINEAR
      }
    }
  }.palletteToolbarStyle()


  val keysButton = imageButton {
    imageResource = R.drawable.icons8_piano_100
    backgroundResource = R.drawable.toolbar_button
    padding = dip(7)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      if (viewModel.keyboardView.isHidden) {
        viewModel.showKeyboard()
      } else {
        viewModel.hideKeyboard()
      }
    }
    onLongClick(returnValue = true) {
      keyboardConfigurationAlert.show()
    }
  }.palletteToolbarStyle()

  val colorsButton = imageButton {
    imageResource = R.drawable.colorboard_icon_2
    backgroundResource = R.drawable.toolbar_button
    padding = dip(7)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      if (viewModel.colorboardView.isHidden) {
        viewModel.showColorboard()
      } else {
        viewModel.hideColorboard()
      }
    }
    onLongClick(returnValue = true) {
      colorboardConfigurationAlert.show()
    }
  }.palletteToolbarStyle()

  fun updateInstrumentButtonPaddings() {
    keysButton.padding = dip(7)
    colorsButton.padding = dip(7)
    orbifoldButton.padding = dip(10)
  }

  lateinit var orbifoldButton: ImageButton private set
  lateinit var orbifoldText: TextView private set
  private val orbifoldArea = relativeLayout {
    orbifoldButton = imageButton {
      //imageResource = R.drawable.metronome_thin//noun_metronome_415494_000000
      backgroundResource = R.drawable.toolbar_button
      padding = dip(10)
      imageAlpha = 127
      scaleType = ImageView.ScaleType.FIT_CENTER
      onClick {
        if (viewModel.orbifold.isHidden) {
          viewModel.showOrbifold()
        } else {
          viewModel.hideOrbifold()
        }
      }
      onLongClick(returnValue = true) {
        orbifoldConfigurationAlert.show()
      }
    }.lparams(matchParent, matchParent)
    orbifoldText = textView {
      text = "C Chrom."
      setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8f)
      singleLine = true
      typeface = MainApplication.chordTypeface
      textScaleX = 0.9f
      backgroundResource = R.drawable.orbifold_button_bg
      padding = dip(3)
      textColor = color(R.color.major)
      minimumWidth = dip(24)
      gravity = Gravity.CENTER
    }.lparams(wrapContent, wrapContent) {
      centerInParent()
      leftMargin = dip(2)
      rightMargin = dip(2)
    }
  }.palletteToolbarStyle()

}
