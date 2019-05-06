package com.jonlatane.beatpad.view.palette

import BeatClockPaletteConsumer
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintSet.PARENT_ID
import android.view.View
import android.widget.*
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.MainApplication.Companion.chordTypefaceBold
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.output.service.PlaybackService
import com.jonlatane.beatpad.util.color
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.view.NumberPickerWithTypeface
import com.jonlatane.beatpad.view.numberPickerWithTypeface
import com.jonlatane.beatpad.view.tempo.TempoTracking
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick


class PaletteToolbar(ctx: Context,
                     val viewModel: PaletteViewModel) : _LinearLayout(ctx), AnkoLogger {


  private val metronomeImage = context.resources.getDrawable(R.drawable.noun_metronome_415494_000000, null).apply {
    setBounds(0, 0, 60, 60)
  }

  init {
    orientation = LinearLayout.HORIZONTAL
    backgroundColor = context.color(R.color.colorPrimaryDark)
//    arrayOf(playImage, loopImage, stopImage, metronomeImage, keyboardImage, unicornImage, mixerImage).forEach {
//      it.setBounds(0, 0, 80, 80)
//    }
  }

  fun <T: View> T.palletteToolbarStyle(): T = this.lparams(matchParent, dip(48)) {
    weight = 1f
  }

  val playButton = imageButton {
    imageResource = if(PlaybackService.instance?.isStopped != false) R.drawable.icons8_play_100
      else R.drawable.icons8_skip_to_start_filled_100
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      imageResource = R.drawable.icons8_skip_to_start_filled_100
      val startIntent = Intent(MainApplication.instance, PlaybackService::class.java)
      startIntent.action = PlaybackService.Companion.Action.PLAY_ACTION
      BeatClockPaletteConsumer.tickPosition = 0
      MainApplication.instance.startService(startIntent)
    }
  }.palletteToolbarStyle()

  val stopButton = imageButton {
    imageResource = R.drawable.icons8_stop_100
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      val startIntent = Intent(MainApplication.instance, PlaybackService::class.java)
      startIntent.action = PlaybackService.Companion.Action.PAUSE_ACTION
      MainApplication.instance.startService(startIntent)
    }
  }.palletteToolbarStyle()

  lateinit var tempoText: TextView private set
  lateinit var tempoTapper: ImageButton private set
  val tempoArea = relativeLayout {
    tempoTapper = imageButton {
      imageResource = R.drawable.noun_metronome_415494_000000
      imageAlpha = 127
      scaleType = ImageView.ScaleType.FIT_CENTER
      onLongClick(returnValue = true) {
        showTempoPicker()
      }
    }.lparams(matchParent, dip(48))
    tempoText = textView {
      textSize = 18f
      textColor = 0xFF000000.toInt()
      typeface = chordTypefaceBold
      //textColor = android.R.color.black
    }.lparams(wrapContent, wrapContent) {
      alignParentTop()
      alignParentRight()
      topMargin = dip(5)
      rightMargin = dip(7)
    }
  }.palletteToolbarStyle()

  init {
    TempoTracking.trackTempo(tempoTapper) { tempo: Float ->
      info("onTempoChanged: $tempo")
      val bpm = Math.round(tempo)
      if (bpm > 20) {
        BeatClockPaletteConsumer.palette?.bpm = bpm.toFloat()
        updateTempoButton()
      }
    }
  }

  val keysButton = imageButton {
    imageResource = R.drawable.icons8_piano_100
    scaleType = ImageView.ScaleType.FIT_CENTER
  }.palletteToolbarStyle().onClick {
    if (viewModel.keyboardView.isHidden) {
      viewModel.backStack.push {
        if(!viewModel.keyboardView.isHidden) {
          viewModel.keyboardView.hide()
          true
        } else false
      }
      viewModel.keyboardView.show()
    } else {
      viewModel.keyboardView.hide()
    }
  }

  val colorsButton = imageButton {
    imageResource = R.drawable.colorboard_icon_2
    scaleType = ImageView.ScaleType.FIT_CENTER
  }.palletteToolbarStyle().onClick {
    if (viewModel.colorboardView.isHidden) {
      viewModel.backStack.push {
        if(!viewModel.colorboardView.isHidden) {
          viewModel.colorboardView.hide()
          true
        } else false
      }
      viewModel.colorboardView.show()
    } else {
      viewModel.colorboardView.hide()
    }
  }

  val splatButton = imageButton {
    imageResource = R.drawable.icons8_molecule_filled_100
    scaleType = ImageView.ScaleType.FIT_CENTER
  }.palletteToolbarStyle().onClick {
    if (viewModel.orbifold.isHidden) {
      viewModel.showOrbifold()
    } else {
      viewModel.hideOrbifold()
    }
  }

  val volumeButton = imageButton {
    imageResource = R.drawable.icons8_tune_100
    scaleType = ImageView.ScaleType.FIT_CENTER
  }.palletteToolbarStyle().onClick {
    viewModel.editingMix = !viewModel.editingMix
    if(viewModel.editingMix) {
      post {
        viewModel.editingMelody = null
      }
    }
  }


  private fun showTempoPicker() {
    context.alert {
      customView {
        constraintLayout {
          val title = textView("Choose Tempo") {
            id = View.generateViewId()
            typeface = chordTypefaceBold
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
              updateTempoButton()
            }
          }

          applyConstraintSet {
            title {
              connect(
                TOP to TOP of PARENT_ID margin dip(15),
                START to START of PARENT_ID margin dip(15),
                END to END of PARENT_ID margin dip(15)
              )
            }
            picker {
              connect(
                TOP to BOTTOM of title margin dip(15),
                START to START of PARENT_ID margin dip(15),
                END to END of PARENT_ID margin dip(15),
                BOTTOM to BOTTOM  of PARENT_ID margin dip(15)
              )
            }
          }
        }
      }
    }.show()

//    val dialog = Dialog(context)
//    dialog.setTitle("Select Tempo")
//    dialog.setContentView(R.layout.dialog_choose_tempo)
//    val picker = dialog.findViewById<NumberPicker>(R.id.numberPicker1)
//    picker.maxValue = 960
//    picker.minValue = 15
//    picker.value = BeatClockPaletteConsumer.palette?.bpm?.toInt()!!
//    picker.wrapSelectorWheel = false
//    picker.setOnValueChangedListener { _, _, _ ->
//      val bpm = picker.value
//      BeatClockPaletteConsumer.palette?.bpm = bpm.toFloat()
//      updateTempoButton()
//    }
//    dialog.show()
  }

  fun updateTempoButton() {
    tempoText.text = "${BeatClockPaletteConsumer.palette!!.bpm.toInt()}"
  }
}
