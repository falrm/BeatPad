package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord
import kotlinx.serialization.Serializable

/**
 * A [Palette] is simply a collection of unrelated [Chord]s
 * and [Part]s.
 */
@Serializable
class Palette (
  val chords: MutableList<Chord> = mutableListOf(),
  val parts: MutableList<Part> = mutableListOf(),
  val harmonies: MutableList<Harmony> = mutableListOf()
)