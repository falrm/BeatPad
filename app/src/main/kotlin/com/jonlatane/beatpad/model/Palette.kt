package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord

/**
 * A [Palette] is simply a collection of unrelated [Chord]s
 * and [Part]s.
 */
class Palette (
  val chords: MutableList<Chord> = mutableListOf(),
  val parts: MutableList<Part> = mutableListOf(),
  val harmonies: MutableList<Harmony> = mutableListOf()
)