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
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.NonDelayedRecyclerView
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
  override val viewModel: PaletteViewModel
) : HideableRelativeLayout(configurationContext), AnkoLogger, OrbifoldConfiguration, ColorboardConfiguration, KeyboardConfiguration, Storage, HideableView {
  override val storageContext: Context get() = configurationContext

  var soloPart: Part? = null
    set(value) {
      field = value
      soloPartChooser.updateSmartHolders()
      viewModel.melodyBeatAdapter.viewType0 = value?.let { ViewType.PartView(it) }
        ?: ViewType.Unused
    }
  var showAccompaniment = true
    set(value) {
      field = value
      viewModel.melodyBeatAdapter.viewType1 = if (value) ViewType.OtherNonDrumParts else ViewType.Unused
    }
  var showDrums = true
    set(value) {
      field = value
      viewModel.melodyBeatAdapter.viewType2 = if (value) ViewType.DrumPart else ViewType.Unused
    }
  private val soloPartChooser: NonDelayedRecyclerView
  private val accompanimentButton: TextView
  private val drumsButton: TextView
  private val soloParts get() = viewModel.palette.parts.filter { !it.drumTrack }
  private fun TextView.label(): TextView = apply {
    typeface = MainApplication.chordTypefaceBold
    textSize = 10f
    textColor = color(R.color.white)
  }
  init {
    val soloStaffLabel: TextView = textView("Solo Staff") {
      id = View.generateViewId()
    }.label()
    soloPartChooser = InstaRecycler.instaRecycler(
      context,
      factory = { nonDelayedRecyclerView().apply { id = View.generateViewId() } },
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
          alpha = if (part == soloPart) 1f else 0.5f
          horizontalPadding = dip(5)
          isClickable = true
          onClick {
            if (soloPart != part) {
              soloPart = part
            } else {
              soloPart = null
            }
          }
        }
      }
    ).apply {
      id = View.generateViewId()
      layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }
    val otherStavesLabel: TextView = textView("Other Staves") {
      id = View.generateViewId()
    }.label()
    accompanimentButton = textView {
      id = View.generateViewId()
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
        alpha = if (showAccompaniment) 1f else 0.5f
      }
    }
    drumsButton = textView {
      id = View.generateViewId()
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
        alpha = if (showDrums) 1f else 0.5f
      }
    }
    soloStaffLabel.lparams(wrapContent, wrapContent) {
      alignParentTop()
      alignParentLeft()
    }
    soloPartChooser.lparams(matchParent, matchParent) {
      below(soloStaffLabel)
      alignParentLeft()
      leftOf(accompanimentButton)
    }
    drumsButton.lparams(wrapContent, matchParent) {
      alignParentRight()
      alignParentBottom()
      below(otherStavesLabel)
    }
    accompanimentButton.lparams(wrapContent, matchParent) {
      leftOf(drumsButton)
      alignParentBottom()
      below(otherStavesLabel)
    }
    otherStavesLabel.lparams(wrapContent, wrapContent) {
      alignParentTop()
      rightOf(soloPartChooser)
    }
  }

  fun notifyPartsChanged() {
    soloPartChooser.adapter?.notifyDataSetChanged()
  }
}
