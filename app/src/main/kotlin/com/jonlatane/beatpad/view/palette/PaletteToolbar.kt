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

class PaletteToolbar(ctx: Context,
                     val viewModel: PaletteViewModel) : _LinearLayout(ctx), AnkoLogger {
  init {
    orientation = LinearLayout.HORIZONTAL
    backgroundColor = context.color(R.color.colorPrimaryDark)
  }

  val playButton = button {
    text = "Play"
    toolbarStyle()
    onClick {
      val startIntent = Intent(MainApplication.instance, PlaybackService::class.java)
      startIntent.action = PlaybackService.Companion.Action.PLAY_ACTION
      BeatClockPaletteConsumer.tickPosition = 0
      MainApplication.instance.startService(startIntent)
    }
  }.lparams {
    width = matchParent
    height = wrapContent
    weight = 1f
  }

  val stopButton = button {
    text = "Stop"
    toolbarStyle()
    onClick {
      val startIntent = Intent(MainApplication.instance, PlaybackService::class.java)
      startIntent.action = PlaybackService.Companion.Action.PAUSE_ACTION
      MainApplication.instance.startService(startIntent)
    }
  }.lparams {
    width = matchParent
    height = wrapContent
    weight = 1f
  }

  val tempoTapper = button {
    text = ""
    toolbarStyle()
    onLongClick(returnValue = true) {
      showTempoPicker()
    }
  }.lparams {
    width = matchParent
    height = wrapContent
    weight = 1f
  }

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

  val keysButton = button {
    text = "Keys"
    toolbarStyle()
  }.lparams {
    width = matchParent
    height = wrapContent
    weight = 1f
  }.onClick {
    if (viewModel.keyboardView.isHidden)
      viewModel.keyboardView.show()
    else
      viewModel.keyboardView.hide()
  }

  val colorsButton = button {
    text = "Colors"
    toolbarStyle()
  }.lparams {
    width = matchParent
    height = wrapContent
    weight = 1f
  }.onClick {
    if (viewModel.colorboardView.isHidden)
      viewModel.colorboardView.show()
    else
      viewModel.colorboardView.hide()
  }

  val volumeButton = button {
    text = "Mix"
    toolbarStyle()
  }.lparams {
    width = matchParent
    height = wrapContent
    weight = 1f
  }.onClick {
    viewModel.partListAdapter?.let {
      it.editingMix = !it.editingMix
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
    tempoTapper.text = "${BeatClockPaletteConsumer.palette!!.bpm.toInt()}BPM"
  }
}
