package com.jonlatane.beatpad.view.harmony

import BeatClockPaletteConsumer
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.view.ZoomableRecyclerView
import com.jonlatane.beatpad.view.palette.PaletteViewModel

open class HarmonyViewModel {
  var paletteViewModel: PaletteViewModel? = null
  var harmonyView: HarmonyView? = null
  lateinit var beatAdapter: HarmonyBeatAdapter
  //var selectedChord: Chord? = null
  var harmonyElementRecycler: ZoomableRecyclerView? = null
  set(value) {
    field = value
    notifyHarmonyChanged()
  }
  val harmony: Harmony? get() = BeatClockPaletteConsumer.harmony
  var isEditingChord: Boolean = false
  set(value) {
    field = value
    if(!value) selectedHarmonyElements = null
  }
  var selectedHarmonyElements: IntRange? = null
  set(value) {
    field = value
    notifyHarmonyChanged()
    beatAdapter.notifyDataSetChanged()
  }
  var editingChord: Chord?
  get() = selectedHarmonyElements?.let {
    harmony!!.changeBefore(it.first)
  }
  set(value) {
    selectedHarmonyElements?.let {
      harmony!!.changes[it.first] = value
    }
    notifyHarmonyChanged()
  }
  fun notifyHarmonyChanged() {
    beatAdapter.notifyDataSetChanged()
    harmonyView?.syncScrollingChordText()
  }
}