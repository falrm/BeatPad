package com.jonlatane.beatpad

import BeatClockPaletteConsumer
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.view.View
import android.view.WindowManager
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.output.service.PlaybackService
import com.jonlatane.beatpad.sensors.ShakeDetector
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.*
//import com.jonlatane.beatpad.util.show
import com.jonlatane.beatpad.view.InstrumentConfiguration
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.palette.PaletteUI
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import com.jonlatane.beatpad.view.palette.PartHolder
import com.jonlatane.beatpad.view.palette.partListView
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import java.net.URI
import java.util.*


class PaletteEditorActivity : Activity(), Storage, AnkoLogger, InstrumentConfiguration {
  override val configurationContext: Context get() = this
  override val storageContext: Context get() = this
  private lateinit var ui: PaletteUI
  override val viewModel get() = ui.viewModel
  private var lastBackPress: Long? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ui = PaletteUI(viewModel = PaletteViewModel(storageContext)).also {
      it.setContentView(this)
    }

    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


    val palette = BeatClockPaletteConsumer.palette ?: {
      val storedPalette = loadPalette() ?: PaletteStorage.basePalette
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
          val newPalette = PaletteStorage.basePalette
          BeatClockPaletteConsumer.palette = newPalette
          viewModel.palette = newPalette
        }
      }
    }


    // Open melody from intent
    MainApplication.intentMelody?.let { intentMelody ->
      MainApplication.intentMelody = null
      openMelodyAlert(intentMelody)
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
    if (savedInstanceState.getBoolean("orbifoldOpen", false)) {
      viewModel.showOrbifold(false)
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
    storePalette(viewModel.palette)
    outState.putString("melodyDisplayType", viewModel.melodyViewModel.displayType.name)
    outState.putBoolean("keyboardOpen", !viewModel.keyboardView.isHidden)
    outState.putBoolean("colorboardOpen", !viewModel.colorboardView.isHidden)
    outState.putBoolean("orbifoldOpen", !viewModel.orbifold.isHidden)
    outState.putString("editingMelodyId", viewModel.editingMelody?.id.toString())
    outState.putInt("beatWidth", viewModel.melodyBeatAdapter.elementWidth)
    outState.putInt("beatHeight", viewModel.melodyBeatAdapter.elementHeight)
  }


  fun openMelodyAlert(melody: Melody<*>) {
    lateinit var alert: DialogInterface
    val alertBuilder = configurationContext.alert {
      customView {
        constraintLayout {
          val title = textView("Insert Melody") {
            id = View.generateViewId()
            typeface = MainApplication.chordTypefaceBold
            textSize = 18f
          }
          val recycler = instrumentPartPicker(
            viewModel.palette.parts,
            getSelectedPart = { Part() }, // Won't match any parts
            setSelectedPart = { selectedPart ->
              viewModel.partListView.viewHolders<PartHolder>().find {
                it.part == selectedPart
              }?.let {
                it.melodyReferenceAdapter.createAndOpenDrawnMelody(melody)
              } ?: {
                while(viewModel.palette.parts.flatMap { it.melodies }.any { it.id == melody.id }) {
                  melody.relatedMelodies.add(melody.id)
                  melody.id = UUID.randomUUID()
                }
                selectedPart.melodies.add(melody)
                viewModel.palette.parts.indexOfFirst { it == selectedPart }
                  .takeIf { it >= 0 }?.let {index ->
                  viewModel.partListAdapter?.notifyItemChanged(index)
                }

              }()
              alert.cancel()
            }
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
    alert = alertBuilder.show()
  }
}