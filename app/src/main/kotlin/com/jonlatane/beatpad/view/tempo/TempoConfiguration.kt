package com.jonlatane.beatpad.view.tempo

import android.support.constraint.ConstraintSet
import android.view.View
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.view.BaseConfiguration
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout

interface TempoConfiguration: BaseConfiguration {
  fun updateTempoDisplay()
  val tempoConfigurationAlert get() = configurationContext.alert {
    customView {
      constraintLayout {
        val title = textView("Tempo") {
          id = View.generateViewId()
          typeface = MainApplication.chordTypefaceBold
          textSize = 18f
        }
        val picker = numberPicker {
          id = View.generateViewId()
//            textSize = 16f
          maxValue = 960
          minValue = 15
          value = BeatClockPaletteConsumer.palette?.bpm?.toInt()!!
          wrapSelectorWheel = false
          setOnValueChangedListener { _, _, _ ->
            val bpm = value
            BeatClockPaletteConsumer.palette?.bpm = bpm.toFloat()
            updateTempoDisplay()
          }
        }

        applyConstraintSet {
          title {
            connect(
              ConstraintSetBuilder.Side.TOP to ConstraintSetBuilder.Side.TOP of ConstraintSet.PARENT_ID margin dip(15),
              ConstraintSetBuilder.Side.START to ConstraintSetBuilder.Side.START of ConstraintSet.PARENT_ID margin dip(15),
              ConstraintSetBuilder.Side.END to ConstraintSetBuilder.Side.END of ConstraintSet.PARENT_ID margin dip(15)
            )
          }
          picker {
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