package com.jonlatane.beatpad.view.midi

import android.support.constraint.ConstraintSet
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.view.BaseConfiguration
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import android.support.constraint.ConstraintSet.PARENT_ID

interface MidiOutputConfiguration: BaseConfiguration {
  val midiOutputConfigurationAlert get() = configurationContext.alert {
    customView {
      constraintLayout {
        val title = textView("MIDI Output Settings") {
          id = View.generateViewId()
          typeface = MainApplication.chordTypefaceBold
          textSize = 18f
          gravity = Gravity.CENTER_HORIZONTAL
        }

        val playToSonivoxCheckbox: CheckBox
        val playToSonivoxText: TextView
        lateinit var playToExternalSynthsCheckbox: CheckBox
        val playToExternalSynthsText: TextView

        playToSonivoxCheckbox = checkBox{
          id = View.generateViewId()
          isChecked = AndroidMidi.sendToInternalSynth
          onCheckedChange { _, isChecked ->
            AndroidMidi.sendToInternalSynth = isChecked
          }
        }.lparams(wrapContent, wrapContent)

        playToSonivoxText = textView{
          id = View.generateViewId()
          text = "Output to onboard (ringtone) synth"
          typeface = MainApplication.chordTypeface
          singleLine = true
          gravity = Gravity.START
          isClickable = true
          onClick {
            playToSonivoxCheckbox.isChecked = !playToSonivoxCheckbox.isChecked
          }
        }.lparams(0, wrapContent)

        playToExternalSynthsCheckbox = checkBox {
          id = View.generateViewId()
          isChecked = AndroidMidi.sendToExternalSynth
          onCheckedChange { _, isChecked ->
            AndroidMidi.sendToExternalSynth = isChecked
          }
        }.lparams(wrapContent, wrapContent)

        playToExternalSynthsText = textView{
          id = View.generateViewId()
          text = "Output to external synths"
          typeface = MainApplication.chordTypeface
          singleLine = true
          gravity = Gravity.START
          isClickable = true
          onClick {
            playToExternalSynthsCheckbox.isChecked = !playToExternalSynthsCheckbox.isChecked
          }
        }.lparams(0, wrapContent)

        applyConstraintSet {
          title {
            connect(
              TOP to TOP of PARENT_ID margin dip(15),
              START to START of PARENT_ID margin dip(15),
              END to END of PARENT_ID margin dip(15)
            )
          }
          playToSonivoxCheckbox {
            connect(
              TOP to BOTTOM of  title margin dip(15),
              START to START of PARENT_ID margin dip(15)
            )
          }
          playToSonivoxText {
            connect(
              TOP to TOP of playToSonivoxCheckbox,
              START to END of playToSonivoxCheckbox margin dip(15),
              END to END of PARENT_ID margin dip(15),
              BOTTOM to BOTTOM of playToSonivoxCheckbox
            )
          }
          playToExternalSynthsCheckbox {
            connect(
              TOP to BOTTOM of playToSonivoxCheckbox margin dip(15),
              START to START of PARENT_ID margin dip(15),
              BOTTOM to BOTTOM of PARENT_ID margin dip(15)
            )
          }
          playToExternalSynthsText {
            connect(
              TOP to TOP of playToExternalSynthsCheckbox,
              START to END of playToExternalSynthsCheckbox margin dip(15),
              END to END of PARENT_ID margin dip(15),
              BOTTOM to BOTTOM of playToExternalSynthsCheckbox
            )
          }
        }
      }
    }
  }

}