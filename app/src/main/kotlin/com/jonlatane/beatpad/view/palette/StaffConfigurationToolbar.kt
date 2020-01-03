package com.jonlatane.beatpad.view.palette

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.HideableView
import com.jonlatane.beatpad.util.InstaRecycler
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.smartrecycler.updateSmartHolders
import com.jonlatane.beatpad.view.colorboard.ColorboardConfiguration
import com.jonlatane.beatpad.view.keyboard.KeyboardConfiguration
import com.jonlatane.beatpad.view.melody.renderer.BaseMelodyBeatRenderer.ViewType
import com.jonlatane.beatpad.view.melody.toolbar.Toolbar
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import com.jonlatane.beatpad.view.orbifold.OrbifoldConfiguration
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class StaffConfigurationToolbar(
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

  var soloPart: Part? = null
  set(value) {
    field = value
    soloPartChooser.updateSmartHolders()
    viewModel.melodyBeatAdapter.viewType0 = value?.let { ViewType.PartView(it) } ?: ViewType.Unused
  }
  private val soloParts get() = viewModel.palette.parts.filter { !it.drumTrack }

  val soloPartChooser = InstaRecycler.instaRecycler(
    context,
    factory = { nonDelayedRecyclerView() },
    holderViewFactory = {
      TextView(this.context)
        .apply {
          id = InstaRecycler.example_id
          textSize = 20f
          backgroundResource = R.drawable.part_background
          singleLine = true
          ellipsize = TextUtils.TruncateAt.MARQUEE
          marqueeRepeatLimit = -1
          isSelected = true
          typeface = MainApplication.chordTypeface
          textScaleX = 0.9f
          gravity = Gravity.CENTER
          width = dip(120)
        }.lparams(wrapContent, matchParent)
    },
    itemCount = { soloParts.size },
    binder = { position ->
      val part = soloParts[position]
      val name = part.instrument.instrumentName
      findViewById<TextView>(InstaRecycler.example_id).apply {
        text = name
        alpha = if(part == soloPart) 1f else 0.5f
        horizontalPadding = dip(5)
        isClickable = true
        onClick {
           if(soloPart != part) {
             soloPart = part
           } else {
             soloPart = null
           }
        }
      }
    }
  ).flexStyle().apply {
    id = View.generateViewId()
    orientation = HORIZONTAL
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
  }

  var showAccompaniment = true
  set(value) {
    field = value
    viewModel.melodyBeatAdapter.viewType1 = if(value) ViewType.OtherNonDrumParts else ViewType.Unused
  }
  val accompanimentButton = textView {
    backgroundResource = R.drawable.part_background
    text = "Accomp."
    textSize = 20f
    backgroundResource = R.drawable.part_background
    singleLine = true
    ellipsize = TextUtils.TruncateAt.MARQUEE
    marqueeRepeatLimit = -1
    isSelected = true
    typeface = MainApplication.chordTypeface
    textScaleX = 0.9f
    gravity = Gravity.CENTER
    maxWidth = dip(150)
    isClickable = true
    horizontalPadding = dip(5)
    onClick {
      showAccompaniment = !showAccompaniment
      alpha = if(showAccompaniment) 1f else 0.5f
    }
  }.lparams(wrapContent, matchParent)

  var showDrums = true
  set(value) {
    field = value
    viewModel.melodyBeatAdapter.viewType2 = if(value) ViewType.DrumPart else ViewType.Unused
  }
  val drumsButton = textView {
    text = "Drums"
    padding = dip(10f)
    textSize = 20f
    backgroundResource = R.drawable.part_background_drum
    singleLine = true
    ellipsize = TextUtils.TruncateAt.MARQUEE
    marqueeRepeatLimit = -1
    isSelected = true
    typeface = MainApplication.chordTypeface
    textScaleX = 0.9f
    gravity = Gravity.CENTER
    maxWidth = dip(150)
    isClickable = true
    horizontalPadding = dip(5)
    onClick {
      showDrums = !showDrums
      alpha = if(showDrums) 1f else 0.5f
    }
  }.lparams(wrapContent, matchParent)

  fun notifyPartsChanged() {
    soloPartChooser.adapter?.notifyDataSetChanged()
  }
}
