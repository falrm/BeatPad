package com.jonlatane.beatpad.view.orbifold

import BeatClockPaletteConsumer.currentSectionDrawable
import android.support.constraint.ConstraintSet
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.util.InstaRecycler
import com.jonlatane.beatpad.util.vibrate
import com.jonlatane.beatpad.view.BaseConfiguration
import com.jonlatane.beatpad.view.InstrumentConfiguration
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.sdk25.coroutines.onClick

interface OrbifoldConfiguration: InstrumentConfiguration {
  val orbifoldConfigurationAlert get() = configurationContext.alert {
    customView {
      constraintLayout {
        val title = textView("Orbifold Configuration") {
          id = View.generateViewId()
          typeface = MainApplication.chordTypefaceBold
          textSize = 18f
        }
        val recycler = instrumentPartPicker(
          viewModel.palette.parts.filter {
            (it.instrument as? MIDIInstrument)?.drumTrack == false
          },
          getSelectedPart = { viewModel.splatPart!! },
          setSelectedPart = { viewModel.splatPart = it }
        ).lparams(matchParent, wrapContent)

        applyConstraintSet {
          title {
            connect(
              ConstraintSetBuilder.Side.TOP to ConstraintSetBuilder.Side.TOP of ConstraintSet.PARENT_ID margin dip(15),
              ConstraintSetBuilder.Side.START to ConstraintSetBuilder.Side.START of ConstraintSet.PARENT_ID margin dip(15),
              ConstraintSetBuilder.Side.END to ConstraintSetBuilder.Side.END of ConstraintSet.PARENT_ID margin dip(15)
            )
          }
          recycler {
            connect(
              ConstraintSetBuilder.Side.TOP to ConstraintSetBuilder.Side.BOTTOM of title margin dip(15),
              ConstraintSetBuilder.Side.START to ConstraintSetBuilder.Side.START of ConstraintSet.PARENT_ID margin dip(15),
              ConstraintSetBuilder.Side.END to ConstraintSetBuilder.Side.END of ConstraintSet.PARENT_ID margin dip(15),
              ConstraintSetBuilder.Side.BOTTOM to ConstraintSetBuilder.Side.BOTTOM of ConstraintSet.PARENT_ID margin dip(15)
            )
          }
        }
      }
    }
  }

}