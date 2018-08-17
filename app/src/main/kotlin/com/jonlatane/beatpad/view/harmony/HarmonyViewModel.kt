package com.jonlatane.beatpad.view.harmony

import BeatClockPaletteConsumer
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.palette.PaletteViewModel

open class HarmonyViewModel {
  var paletteViewModel: PaletteViewModel? = null
  var harmonyView: HideableRelativeLayout? = null
  var chordAdapter: HarmonyChordAdapter? = null
  val harmony: Harmony? get() = BeatClockPaletteConsumer.section?.harmony
}