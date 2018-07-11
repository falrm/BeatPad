package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord
import java.util.*

/**
 * A [Palette] is simply a collection of unrelated [Chord]s
 * and [Part]s.
 */
class Palette (
  var id: UUID = UUID.randomUUID(),
  val chords: MutableList<Chord> = mutableListOf(),
  val parts: MutableList<Part> = mutableListOf(),

  var keyboardPart: Part? = null,
  var colorboardPart: Part? = null,
  var splatPart: Part? = null
)