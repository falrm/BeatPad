package com.jonlatane.beatpad.view.harmony

import BeatClockPaletteConsumer
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.view.ZoomableRecyclerView
import com.jonlatane.beatpad.view.palette.PaletteViewModel

open class HarmonyViewModel: SelectedChordAnimation {
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
  override var isChoosingHarmonyChord: Boolean = false
  set(value) {
    field = value
    if (value) {
      val wasOrbifoldVisible = paletteViewModel?.orbifold?.isHidden == false
      paletteViewModel?.orbifold?.canEditChords = true
      paletteViewModel?.showOrbifold()
      paletteViewModel?.backStack?.push {
        if (isChoosingHarmonyChord) {
          isChoosingHarmonyChord = false
          if(!wasOrbifoldVisible) paletteViewModel?.hideOrbifold()
          selectedHarmonyElements = null
          true
        } else false
      }
      animateBeatsOfSelectedChord()
    } else {
      paletteViewModel?.orbifold?.canEditChords = false
    }
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
    if(value == null) {
      selectedHarmonyElements = null
    }
    selectedHarmonyElements?.let {
      harmony!!.changes[it.first] = value
    }
    notifyHarmonyChanged()
    paletteViewModel?.melodyBeatAdapter?.notifyDataSetChanged()
  }
  fun notifyHarmonyChanged() {
    beatAdapter.notifyDataSetChanged()
    harmonyView?.syncScrollingChordText()
  }
}