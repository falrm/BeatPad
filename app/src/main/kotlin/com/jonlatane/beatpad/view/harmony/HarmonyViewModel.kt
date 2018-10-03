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
  var selectedChord: Chord? = null
  var harmonyElementRecycler: ZoomableRecyclerView? = null
  set(value) {
    field = value
    beatAdapter.notifyDataSetChanged()
  }
  val harmony: Harmony? get() = BeatClockPaletteConsumer.harmony
  var selectedHarmonyElements: IntRange? = null
  fun notifyHarmonyChanged() {
    beatAdapter.notifyDataSetChanged()
    //harmonyView?.post {
      harmonyView?.syncScrollingChordText()
    //}
  }
}