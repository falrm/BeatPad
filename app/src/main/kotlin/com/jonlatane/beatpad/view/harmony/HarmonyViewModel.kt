package com.jonlatane.beatpad.view.harmony

import BeatClockPaletteConsumer
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.ZoomableRecyclerView
import com.jonlatane.beatpad.view.palette.PaletteViewModel

open class HarmonyViewModel {
  var paletteViewModel: PaletteViewModel? = null
  var harmonyView: HideableRelativeLayout? = null
  var chordAdapter: HarmonyChordAdapter? = null
  var selectedChord: Chord? = null
  var harmonyElementRecycler: ZoomableRecyclerView? = null
  set(value) {
    field = value
    chordAdapter?.notifyDataSetChanged()
  }
  val harmony: Harmony? get() = BeatClockPaletteConsumer.section?.harmony
}