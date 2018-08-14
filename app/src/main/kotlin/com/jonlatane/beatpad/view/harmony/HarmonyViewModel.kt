package com.jonlatane.beatpad.view.harmony

import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Harmony.Element.Change
import com.jonlatane.beatpad.model.Harmony.Element.NoChange
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.harmony.chord.Dom7
import com.jonlatane.beatpad.model.harmony.chord.Maj
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import kotlin.properties.Delegates.observable

open class HarmonyViewModel {
  var paletteViewModel: PaletteViewModel? = null
  var harmony by observable<Harmony?>(createBaseHarmony()) { _, _, _ ->
    chordAdapter?.notifyDataSetChanged()
  }
  var harmonyView: HideableRelativeLayout? = null
  var chordAdapter: HarmonyChordAdapter? = null

  companion object {
    fun createBaseHarmony(): Harmony {
      val change1 = Change(Chord(0, Maj))
      val change2 = Change(Chord(7, Dom7))
      return Harmony(
        elements = mutableListOf(
          change1,           NoChange(change1), NoChange(change1), NoChange(change1),
          NoChange(change1), NoChange(change1), NoChange(change1), NoChange(change1),
          change2,           NoChange(change2), NoChange(change2), NoChange(change2),
          NoChange(change2), NoChange(change2), NoChange(change2), NoChange(change2)
        ),
        subdivisionsPerBeat = 4)
    }
  }
}