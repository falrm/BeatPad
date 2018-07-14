package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.harmony.chord.Maj7

data class Section(
  val melodies: MutableSet<Melody> = mutableSetOf()
) {
  var chord: Chord? = Chord(0, Maj7)
  set(value) {
    harmony = null
    field = value
  }
  var harmony: Harmony? = null
  set(value) {
    chord = null
    field = value
  }
}