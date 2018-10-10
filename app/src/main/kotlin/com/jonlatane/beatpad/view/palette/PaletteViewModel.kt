package com.jonlatane.beatpad.view.palette

import BeatClockPaletteConsumer
import android.view.View
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.output.controller.DeviceOrientationInstrument
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.util.hide
import com.jonlatane.beatpad.view.HideableRecyclerView
import com.jonlatane.beatpad.view.colorboard.ColorboardInputView
import com.jonlatane.beatpad.view.harmony.HarmonyView
import com.jonlatane.beatpad.view.harmony.HarmonyViewModel
import com.jonlatane.beatpad.view.keyboard.KeyboardView
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.orbifold.OrbifoldView
import com.jonlatane.beatpad.view.orbifold.RhythmAnimations
import kotlin.properties.Delegates.observable

/**
 * The PaletteViewModel still assumes we'll only be editing
 * one Melody at a time.
 */
class PaletteViewModel {
  init {
    //BeatClockPaletteConsumer.viewModel = this
  }

  var playbackTick by observable<Int?>(null) { _, old, new ->
    arrayOf(old, new).filterNotNull().map { tickPosition ->
      (tickPosition.toDouble() / BeatClockPaletteConsumer.ticksPerBeat).toInt()
    }.toSet().forEach { melodyBeat ->
      melodyViewModel.beatAdapter.invalidate(melodyBeat)
      harmonyViewModel.beatAdapter.invalidate(melodyBeat)
    }
  }

  val melodyViewModel = MelodyViewModel(this)
  var melodyView
    get() = melodyViewModel.melodyView
    set(value) {
      melodyViewModel.melodyView = value
    }
  var melodyElementAdapter
    get() = melodyViewModel.beatAdapter
    set(value) {
      melodyViewModel.beatAdapter = value
    }

  val harmonyViewModel = HarmonyViewModel()
    .apply { paletteViewModel = this@PaletteViewModel }
  var harmonyView: HarmonyView
    get() = harmonyViewModel.harmonyView!!
    set(value) { harmonyViewModel.harmonyView = value }

  lateinit var orbifold: OrbifoldView

  var palette: Palette by observable(initialValue = Palette()) { _, _, new ->
    editingMelody = null
    if (new.parts.isEmpty()) {
      new.parts.add(Part())
    }
    if (new.parts.isEmpty()) {
      new.sections.add(Section(harmony = PaletteStorage.baseHarmony))
    }
    keyboardPart = new.keyboardPart ?: new.parts[0]
    colorboardPart = new.colorboardPart ?: new.parts[0]
    splatPart = new.splatPart ?: new.parts[0]
    orbifold.orbifold = new.orbifold
    orbifold.chord = new.chord
    toolbarView.updateTempoButton()
    partListAdapter?.notifyDataSetChanged()
    sectionListAdapter?.notifyDataSetChanged()
    if(BeatClockPaletteConsumer.section == null) {
      BeatClockPaletteConsumer.section = new.sections.first()
    }
  }

  var editingMelody: Melody<*>? by observable<Melody<*>?>(null) { _, _, new ->
    if (new != null) {
      melodyViewModel.openedMelody = new
      colorboardView.hide()
      keyboardView.hide()
      editMelodyMode()
    } else {
      partListMode()
    }
  }

  lateinit var sectionListView: View
  var partListAdapter: PartListAdapter? = null
  var sectionListAdapter: SectionListAdapter? = null
  lateinit var partListView: HideableRecyclerView
  lateinit var toolbarView: PaletteToolbar
  lateinit var keyboardView: KeyboardView
  lateinit var colorboardView: ColorboardInputView
  var keyboardPart by observable<Part?>(null) { _, _, new ->
    if (new != null) keyboardView.ioHandler.instrument = new.instrument
    palette.keyboardPart = new
  }
  var colorboardPart: Part? by observable<Part?>(null) { _, _, new ->
    if (new != null) colorboardView.instrument = new.instrument
    palette.colorboardPart = new
  }

  var splatController: DeviceOrientationInstrument? = null
  var splatPart: Part? by observable<Part?>(null) { _, _, new ->
    if (new != null) {
      splatController = DeviceOrientationInstrument(new.instrument).also {
        it.tones = orbifold.chord.getTones()
        RhythmAnimations.wireMelodicControl(orbifold, it)
      }
    }
    palette.splatPart = new
  }

  fun onBackPressed(): Boolean {
    val result = editingMelody != null
    editingMelody = null
    return result
  }

  fun notifySectionChange() {
    harmonyViewModel.notifyHarmonyChanged()
    sectionListAdapter?.notifyDataSetChanged()
    melodyViewModel.beatAdapter.notifyDataSetChanged()
  }

  private fun editMelodyMode() {
    melodyViewModel.melodyView.animate()
      .translationX(0f)
      .alpha(1f)
      .start()
    partListView.animate().alpha(0f)
  }

  private fun partListMode() {
    melodyView.animate()
      .translationX(melodyView.width.toFloat())
      .alpha(0f)
      .start()
    partListView.animate().alpha(1f)
  }
}