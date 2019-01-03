package com.jonlatane.beatpad.view.palette

import BeatClockPaletteConsumer
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.widget.LinearLayout
import android.widget.NumberPicker
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.output.service.PlaybackService
import com.jonlatane.beatpad.util.*
import com.jonlatane.beatpad.view.tempo.TempoTracking
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import android.view.View
import android.widget.ImageView


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

  fun <T: View> T.palletteToolbarStyle() = this.lparams {
    width = matchParent
    height = dip(48)
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

  val tempoTapper = button {
    text = ""
    setCompoundDrawables(metronomeImage, null, null, null)
    toolbarTextStyle()
    onLongClick(returnValue = true) {
      showTempoPicker()
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
    if (viewModel.keyboardView.isHidden)
      viewModel.keyboardView.show()
    else
      viewModel.keyboardView.hide()
  }

  val colorsButton = imageButton {
    imageResource = R.drawable.colorboard_icon_2
    scaleType = ImageView.ScaleType.FIT_CENTER
  }.palletteToolbarStyle().onClick {
    if (viewModel.colorboardView.isHidden)
      viewModel.colorboardView.show()
    else
      viewModel.colorboardView.hide()
  }
  init {
    if(context.configuration.portrait) {
      val splatButton = imageButton {
        imageResource = R.drawable.icons8_molecule_filled_100
        scaleType = ImageView.ScaleType.FIT_CENTER
      }.palletteToolbarStyle().onClick {
        if (viewModel.orbifold.isHidden)
          viewModel.orbifold.show()
        else
          viewModel.orbifold.hide()
      }
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
    val dialog = Dialog(context)
    dialog.setTitle("Select Tempo")
    dialog.setContentView(R.layout.dialog_choose_tempo)
    val picker = dialog.findViewById<NumberPicker>(R.id.numberPicker1)
    picker.maxValue = 960
    picker.minValue = 15
    picker.value = BeatClockPaletteConsumer.palette?.bpm?.toInt()!!
    picker.wrapSelectorWheel = false
    picker.setOnValueChangedListener { _, _, _ ->
      val bpm = picker.value
      BeatClockPaletteConsumer.palette?.bpm = bpm.toFloat()
      updateTempoButton()
    }
    dialog.show()
  }

  fun updateTempoButton() {
    tempoTapper.text = "${BeatClockPaletteConsumer.palette!!.bpm.toInt()}"
  }
}
