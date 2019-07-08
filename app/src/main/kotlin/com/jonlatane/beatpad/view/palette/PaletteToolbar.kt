package com.jonlatane.beatpad.view.palette

import BeatClockPaletteConsumer
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.*
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.MainApplication.Companion.chordTypefaceBold
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.output.service.PlaybackService
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.view.colorboard.ColorboardConfiguration
import com.jonlatane.beatpad.view.keyboard.KeyboardConfiguration
import com.jonlatane.beatpad.view.orbifold.OrbifoldConfiguration
import com.jonlatane.beatpad.view.tempo.TempoConfiguration
import com.jonlatane.beatpad.view.tempo.TempoTracking
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick


class PaletteToolbar(
  override val configurationContext: Context,
  override val viewModel: PaletteViewModel
) : _LinearLayout(configurationContext), AnkoLogger, TempoConfiguration, OrbifoldConfiguration, ColorboardConfiguration, KeyboardConfiguration, Storage {
  override val storageContext: Context get() = configurationContext
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
    backgroundResource = R.drawable.toolbar_button
    padding = dip(7)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      imageResource = R.drawable.icons8_skip_to_start_filled_100
      val startIntent = Intent(MainApplication.instance, PlaybackService::class.java)
      startIntent.action = if(PlaybackService.instance?.isStopped != false) PlaybackService.Companion.Action.PLAY_ACTION
      else PlaybackService.Companion.Action.REWIND_ACTION
      MainApplication.instance.startService(startIntent)
    }
  }.palletteToolbarStyle()

  val stopButton = imageButton {
    imageResource = R.drawable.icons8_stop_100
    backgroundResource = R.drawable.toolbar_button
    padding = dip(7)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      val startIntent = Intent(MainApplication.instance, PlaybackService::class.java)
      startIntent.action = PlaybackService.Companion.Action.STOP_ACTION
      MainApplication.instance.startService(startIntent)
    }
  }.palletteToolbarStyle()

  private lateinit var tempoText: TextView private set
  private lateinit var tempoButton: ImageButton private set
  private val tempoArea = relativeLayout {
    tempoButton = imageButton {
      imageResource = R.drawable.noun_metronome_415494_000000
      backgroundResource = R.drawable.toolbar_button
      padding = dip(10)
      imageAlpha = 127
      scaleType = ImageView.ScaleType.FIT_CENTER
      onLongClick(returnValue = true) {
        tempoConfigurationAlert.show()
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
    TempoTracking.trackTempo(tempoButton) { tempo: Float ->
      info("onTempoChanged: $tempo")
      val bpm = Math.round(tempo)
      if (bpm > 20) {
        BeatClockPaletteConsumer.palette?.bpm = bpm.toFloat()
        updateTempoDisplay()
      }
    }
  }

  val keysButton = imageButton {
    imageResource = R.drawable.icons8_piano_100
    backgroundResource = R.drawable.toolbar_button
    padding = dip(7)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      if (viewModel.keyboardView.isHidden) {
        viewModel.showKeyboard()
      } else {
        viewModel.hideKeyboard()
      }
    }
    onLongClick(returnValue = true) {
      keyboardConfigurationAlert.show()
    }
  }.palletteToolbarStyle()

  val colorsButton = imageButton {
    imageResource = R.drawable.colorboard_icon_2
    backgroundResource = R.drawable.toolbar_button
    padding = dip(7)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      if (viewModel.colorboardView.isHidden) {
        viewModel.showColorboard()
      } else {
        viewModel.hideColorboard()
      }
    }
    onLongClick(returnValue = true) {
      colorboardConfigurationAlert.show()
    }
  }.palletteToolbarStyle()

  fun updateInstrumentButtonPaddings() {
    keysButton.padding = dip(7)
    colorsButton.padding = dip(7)
    orbifoldButton.padding = dip(10)
  }

  val orbifoldButton = imageButton {
    imageResource = R.drawable.icons8_molecule_filled_100
    backgroundResource = R.drawable.toolbar_button
    padding = dip(10)
    backgroundResource = R.drawable.toolbar_button
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      if (viewModel.orbifold.isHidden) {
        viewModel.showOrbifold()
      } else {
        viewModel.hideOrbifold()
      }
    }
    onLongClick(returnValue = true) {
      orbifoldConfigurationAlert.show()
    }
  }.palletteToolbarStyle()

  val volumeButton = imageButton {
    imageResource = R.drawable.icons8_tune_100
    backgroundResource = R.drawable.toolbar_button
    padding = dip(10)
    backgroundResource = R.drawable.toolbar_button
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      viewModel.editingMix = !viewModel.editingMix
      if(viewModel.editingMix) {
        post {
          viewModel.editingMelody = null
        }
      }
    }
  }.palletteToolbarStyle()


//  val shareButton = imageButton {
//    imageResource = R.drawable.ic_share_black_24dp
//    scaleType = ImageView.ScaleType.FIT_CENTER
//    onClick {
//      val text = BeatClockPaletteConsumer.palette?.toURI()?.toString() ?: ""
//      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//      val clip = ClipData.newPlainText("BeatScratch Palette", text)
//      clipboard.primaryClip = clip
//
//      context.toast("Copied BeatScratch Palette data to clipboard!")
//    }
//  }.palletteToolbarStyle()

  override fun updateTempoDisplay() {
    tempoText.text = "${BeatClockPaletteConsumer.palette!!.bpm.toInt()}"
  }
}
