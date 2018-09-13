package com.jonlatane.beatpad.view.beat

import android.view.View
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.view.ZoomableRecyclerView
import com.jonlatane.beatpad.view.harmony.HarmonyChordAdapter
import com.jonlatane.beatpad.view.harmony.HarmonyView
import com.jonlatane.beatpad.view.palette.PaletteViewModel

interface BeatViewModel {
  var beatScrollingArea: View
  var axis: View?
//  var paletteViewModel: PaletteViewModel? = null
//  var harmonyView: HarmonyView? = null
//  var chordAdapter: HarmonyChordAdapter? = null
//  var selectedChord: Chord? = null
//  var harmonyElementRecycler: ZoomableRecyclerView? = null
//    set(value) {
//      field = value
//      chordAdapter?.notifyDataSetChanged()
//    }
//  val harmony: Harmony? get() = BeatClockPaletteConsumer.section?.harmony
//  var selectedHarmonyElements: IntRange? = null
}