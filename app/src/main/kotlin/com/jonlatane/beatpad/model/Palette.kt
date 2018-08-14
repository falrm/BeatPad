package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.Orbifold
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.harmony.chord.Maj7
import java.util.*

/**
 * A [Palette] is simply a collection of unrelated [Chord]s
 * and [Part]s. It's, conveniently, basically a full encapsulation of the
 * state of the app's UI
 */
class Palette (
  var id: UUID = UUID.randomUUID(),
  val sections: MutableList<Section> = mutableListOf(Section()),
  val parts: MutableList<Part> = mutableListOf(),

  var keyboardPart: Part? = null,
  var colorboardPart: Part? = null,
  var splatPart: Part? = null,

  var bpm: Float = 120f,
  var orbifold: Orbifold = Orbifold.funkyChainsmokingAnimals,
  var chord: Chord = Chord(0, Maj7)
)