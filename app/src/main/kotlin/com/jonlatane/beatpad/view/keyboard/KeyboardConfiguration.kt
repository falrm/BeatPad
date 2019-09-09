package com.jonlatane.beatpad.view.keyboard

import android.support.constraint.ConstraintSet
import android.view.View
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.view.InstrumentConfiguration
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout

interface KeyboardConfiguration: InstrumentConfiguration {
  val keyboardConfigurationAlert get() = configurationContext.alert {
    customView {
      constraintLayout {
        val title = textView("Keyboard Configuration") {
          id = View.generateViewId()
          typeface = MainApplication.chordTypefaceBold
          textSize = 18f
        }
        val recycler = instrumentPartPicker(
          viewModel.palette.parts,
          getSelectedPart = { viewModel.keyboardPart!! },
          setSelectedPart = { viewModel.keyboardPart = it }
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