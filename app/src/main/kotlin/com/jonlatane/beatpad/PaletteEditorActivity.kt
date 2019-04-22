package com.jonlatane.beatpad

import BeatClockPaletteConsumer
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.output.service.PlaybackService
import com.jonlatane.beatpad.sensors.ShakeDetector
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.formatted
import com.jonlatane.beatpad.util.isHidden
//import com.jonlatane.beatpad.util.show
import com.jonlatane.beatpad.util.vibrate
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.palette.PaletteUI
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast
import java.util.*


class PaletteEditorActivity : Activity(), Storage, AnkoLogger {
  private lateinit var ui: PaletteUI
  private val viewModel get() = ui.viewModel
  private var lastBackPress: Long? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ui = PaletteUI().also {
      it.setContentView(this)
    }

    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


    val palette = BeatClockPaletteConsumer.palette ?: {
      val storedPalette = loadPalette()
      BeatClockPaletteConsumer.palette = storedPalette
      storedPalette
    }()
    viewModel.palette = palette

    val bundle = savedInstanceState ?: try {
      intent.extras.getBundle("playgroundState")
    } catch (t: Throwable) {
      savedInstanceState
    }

    if (bundle != null) {
      println("Got intent with extras: ${bundle.formatted()}")
      onRestoreInstanceState(bundle)
    }
  }

  override fun onBackPressed() {
    when {
      viewModel.onBackPressed() -> {
      }
      lastBackPress?.let { System.currentTimeMillis() - it < 3000 } ?: false -> {
        val intent = Intent(this, PlaybackService::class.java)
        intent.action = PlaybackService.Companion.Action.STOPFOREGROUND_ACTION
        startService(intent)
        super.onBackPressed()
      }
      else -> {
        lastBackPress = System.currentTimeMillis()
        toast("Press again to confirm exit")
      }
    }
  }

  override fun onResume() {
    super.onResume()
    Intent(MainApplication.instance, PlaybackService::class.java).let {
      it.action = PlaybackService.Companion.Action.STARTFOREGROUND_ACTION
      MainApplication.instance.startService(it)
    }
    BeatClockPaletteConsumer.viewModel = viewModel
    ShakeDetector.onShakeListener = object : ShakeDetector.OnShakeListener {
      override fun onShake() {
        vibrate(150)
        showConfirmDialog(
          this@PaletteEditorActivity,
          "Erase everything to start from scratch?"
        ) {
          val newPalette = Palette()
          BeatClockPaletteConsumer.palette = newPalette
          viewModel.palette = newPalette
        }
      }
    }
  }

  override fun onPause() {
    super.onPause()
    BeatClockPaletteConsumer.viewModel = null
    AudioTrackCache.releaseAll()
    //ui.sequencerThread.stopped = true
    //Storage.storePalette(viewModel.palette, this)
    ShakeDetector.onShakeListener = null
  }

  override fun onDestroy() {
    super.onDestroy()
    BeatClockPaletteConsumer.viewModel = null
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    viewModel.melodyViewModel.displayType = MelodyViewModel.DisplayType.valueOf(
      savedInstanceState.getString(
        "melodyDisplayType",
        MelodyViewModel.DisplayType.COLORBLOCK.name
      )
    )
    if (savedInstanceState.getBoolean("keyboardOpen", false)) {
      viewModel.keyboardView.show(false)
    }
    if (savedInstanceState.getBoolean("colorboardOpen", false)) {
      viewModel.colorboardView.show(false)
    }
    savedInstanceState.getString("editingMelodyId")?.let { melodyId: String ->
      try {
        UUID.fromString(melodyId)
      } catch(_: Throwable) {
        null
      }?.let { melodyUUID ->
        viewModel.palette.parts.flatMap { it.melodies }.firstOrNull { it.id == melodyUUID }
          ?.let { melody ->
            viewModel.melodyView.post { viewModel.editingMelody = melody }
          }
      }
    }

    ui.layout.post {
      viewModel.melodyBeatAdapter.apply {
        elementWidth = savedInstanceState.getInt("beatWidth", elementWidth)
        elementHeight = savedInstanceState.getInt("beatHeight", elementHeight)
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    Storage.storePalette(viewModel.palette, this)
    outState.putString("melodyDisplayType", viewModel.melodyViewModel.displayType.name)
    outState.putBoolean("keyboardOpen", !viewModel.keyboardView.isHidden)
    outState.putBoolean("colorboardOpen", !viewModel.colorboardView.isHidden)
    outState.putString("editingMelodyId", viewModel.editingMelody?.id.toString())
    outState.putInt("beatWidth", viewModel.melodyBeatAdapter.elementWidth)
    outState.putInt("beatHeight", viewModel.melodyBeatAdapter.elementHeight)
  }
}