package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord
import java.util.*

data class Harmony(
  // TODO: Is having [tonic] in a Pattern always good?
  override var tonic: Int = 0,
  override val changes: NavigableMap<Int, Chord> = TreeMap(),
  override var length: Int = 1,
  override var subdivisionsPerBeat: Int = 1
) : Pattern<Chord> {

  override fun transpose(interval: Int) = Harmony(
    tonic,
    TreeMap(changes.mapValues { it.value.transpose(interval) }),
    subdivisionsPerBeat
  )
}