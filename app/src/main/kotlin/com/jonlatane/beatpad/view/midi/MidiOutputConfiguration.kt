package com.jonlatane.beatpad.view.midi

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.constraint.ConstraintSet.PARENT_ID
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.midi.MidiDevices
import com.jonlatane.beatpad.view.BaseConfiguration
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick


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

        val systemHasMidiSupport = MainApplication.instance.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)

        val playToFluidsynthCheckbox: CheckBox
        val playToFluidsynthText: TextView
        val playToSonivoxCheckbox: CheckBox
        val playToSonivoxText: TextView
        lateinit var playToExternalSynthsCheckbox: CheckBox
        val playToExternalSynthsText: TextView

        playToFluidsynthCheckbox = checkBox{
          id = View.generateViewId()
          isChecked = AndroidMidi.sendToInternalFluidSynth
          onCheckedChange { _, isChecked ->
            AndroidMidi.sendToInternalFluidSynth = isChecked
            MidiDevices.refreshInstruments()
          }
        }.lparams(wrapContent, wrapContent)

        playToFluidsynthText = textView {
          id = View.generateViewId()
          text = "Output to BeatScratch FluidSynth"
          typeface = MainApplication.chordTypeface
          gravity = Gravity.START
          isClickable = true
          onClick {
            playToFluidsynthCheckbox.isChecked = !playToFluidsynthCheckbox.isChecked
          }
        }.lparams(0, wrapContent)

        playToSonivoxCheckbox = checkBox{
          id = View.generateViewId()
          isChecked = AndroidMidi.sendToInternalSynth
          onCheckedChange { _, isChecked ->
            AndroidMidi.sendToInternalSynth = isChecked
            MidiDevices.refreshInstruments()
          }
        }.lparams(wrapContent, wrapContent)

        playToSonivoxText = textView {
          id = View.generateViewId()
          text = "Output to onboard Sonivox (ringtone) synth"
          typeface = MainApplication.chordTypeface
          gravity = Gravity.START
          isClickable = true
          onClick {
            playToSonivoxCheckbox.isChecked = !playToSonivoxCheckbox.isChecked
          }
        }.lparams(0, wrapContent)

        playToExternalSynthsCheckbox = checkBox {
          id = View.generateViewId()
          isChecked = systemHasMidiSupport && AndroidMidi.sendToExternalSynth
          isEnabled = systemHasMidiSupport
          onCheckedChange { _, isChecked ->
            AndroidMidi.sendToExternalSynth = isChecked
            MidiDevices.refreshInstruments()
          }
        }.lparams(wrapContent, wrapContent)

        playToExternalSynthsText = textView {
          id = View.generateViewId()
          text = "Output to external synths (MainStage, MIDI keyboards, etc.)"
          typeface = MainApplication.chordTypeface
          gravity = Gravity.START
          isClickable = true
          isEnabled = systemHasMidiSupport
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
          playToFluidsynthCheckbox {
            connect(
              TOP to BOTTOM of  title margin dip(15),
              START to START of PARENT_ID margin dip(15)
            )
          }
          playToFluidsynthText {
            connect(
              TOP to TOP of playToFluidsynthCheckbox,
              START to END of playToFluidsynthCheckbox margin dip(15),
              END to END of PARENT_ID margin dip(15),
              BOTTOM to BOTTOM of playToFluidsynthCheckbox
            )
          }
          playToSonivoxCheckbox {
            connect(
              TOP to BOTTOM of playToFluidsynthCheckbox margin dip(15),
              START to START of PARENT_ID margin dip(15)
            )
          }
          playToSonivoxText {
            connect(
              TOP to BOTTOM of playToFluidsynthText margin dip(15),
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
              TOP to BOTTOM of playToSonivoxText margin dip(15),
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