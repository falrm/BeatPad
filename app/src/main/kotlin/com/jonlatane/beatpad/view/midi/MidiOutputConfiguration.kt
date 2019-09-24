package com.jonlatane.beatpad.view.midi

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.constraint.ConstraintSet.PARENT_ID
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
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
      scrollView {
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

          playToFluidsynthCheckbox = checkBox {
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
            typeface = MainApplication.chordTypefaceBold
            gravity = Gravity.START
            isClickable = true
            onClick {
              playToFluidsynthCheckbox.isChecked = !playToFluidsynthCheckbox.isChecked
            }
          }.lparams(0, wrapContent)

          val fluidSynthSoundFontLabel = textView {
            id = View.generateViewId()
            text = "SoundFont:"
            typeface = MainApplication.chordTypefaceBold
            gravity = Gravity.START
            isClickable = true
            onClick {
              //playToSonivoxCheckbox.isChecked = !playToSonivoxCheckbox.isChecked
            }
          }.lparams(0, wrapContent)

          val fluidSynthSoundFontText = textView {
            id = View.generateViewId()
            text = "AirFont 340 (included).sf2"
            typeface = MainApplication.chordTypeface
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            singleLine = true
            ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
            isSelected = true
            isClickable = true
            onClick {
              //playToSonivoxCheckbox.isChecked = !playToSonivoxCheckbox.isChecked
            }
          }.lparams(0, 0)

          val fluidSynthChangeButton = button {
            id = View.generateViewId()
            text = "Choose"
            typeface = MainApplication.chordTypefaceBold
            isClickable = true
            onClick {
              //playToSonivoxCheckbox.isChecked = !playToSonivoxCheckbox.isChecked
            }
          }.lparams(wrapContent, wrapContent)

          val fluidSynthMusicalArtifactsLink = textView {
            id = View.generateViewId()
            text = "Download *.sf2 SoundFonts from Musical-Artifacts 🌐"
            typeface = MainApplication.chordTypeface
            gravity = Gravity.START
            isClickable = true
            onClick {
              //playToSonivoxCheckbox.isChecked = !playToSonivoxCheckbox.isChecked
            }
          }.lparams(0, wrapContent)

          val fluidSynthLicenseText = textView {
            id = View.generateViewId()
            text = "Source on GitHub 💗"
            typeface = MainApplication.chordTypeface
            gravity = Gravity.START
            isClickable = true
            onClick {
              //playToSonivoxCheckbox.isChecked = !playToSonivoxCheckbox.isChecked
            }
          }.lparams(0, wrapContent)

          playToSonivoxCheckbox = checkBox {
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
            typeface = MainApplication.chordTypefaceBold
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
            typeface = MainApplication.chordTypefaceBold
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
                TOP to BOTTOM of title margin dip(15),
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
            fluidSynthSoundFontLabel {
              connect(
                TOP to BOTTOM of playToFluidsynthText margin dip(15),
                START to END of playToFluidsynthCheckbox margin dip(15),
                END to END of PARENT_ID margin dip(15)
              )
            }
            fluidSynthChangeButton {
              connect(
                TOP to BOTTOM of fluidSynthSoundFontLabel margin dip(15),
                END to END of PARENT_ID margin dip(15)
              )
            }
            fluidSynthSoundFontText {
              connect(
                TOP to TOP of fluidSynthChangeButton,
                BOTTOM to BOTTOM of fluidSynthChangeButton,
                START to END of playToFluidsynthCheckbox margin dip(15),
                END to START of fluidSynthChangeButton margin dip(15)
              )
            }
            fluidSynthMusicalArtifactsLink {
              connect(
                TOP to BOTTOM of fluidSynthSoundFontText margin dip(15),
                TOP to BOTTOM of fluidSynthChangeButton margin dip(15),
                START to END of playToFluidsynthCheckbox margin dip(15),
                END to END of PARENT_ID margin dip(15)
              )
            }
            fluidSynthLicenseText {
              connect(
                TOP to BOTTOM of fluidSynthMusicalArtifactsLink margin dip(15),
                START to END of playToFluidsynthCheckbox margin dip(15),
                END to END of PARENT_ID margin dip(15)
              )
            }
            playToSonivoxCheckbox {
              connect(
                TOP to BOTTOM of fluidSynthLicenseText margin dip(15),
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
                TOP to BOTTOM of playToSonivoxText margin dip(15),
                START to START of PARENT_ID margin dip(15),
                BOTTOM to BOTTOM of PARENT_ID margin dip(15)
              )
            }
            playToExternalSynthsText {
              connect(
                TOP to TOP of playToExternalSynthsCheckbox,
                START to END of playToExternalSynthsCheckbox margin dip(15),
                END to END of PARENT_ID margin dip(15),
                BOTTOM to BOTTOM of playToExternalSynthsCheckbox,
                BOTTOM to BOTTOM of PARENT_ID margin dip(15)
              )
            }
          }
        }
      }
    }
  }

}