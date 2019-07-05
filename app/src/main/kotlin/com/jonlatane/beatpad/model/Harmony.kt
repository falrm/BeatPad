package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.chord.Chord
import java.util.*

data class Harmony(
  // TODO: Is having [tonic] in a Pattern always good?
  override var tonic: Int = 0,
  override val changes: NavigableMap<Int, Chord> = TreeMap(),
  override var length: Int = 1,
  override var subdivisionsPerBeat: Int = 1,
  val meter: Meter = Meter()
) : Pattern<Chord> {
  /**
   * Our Meter class is dead simple.
   */
  data class Meter(
    var defaultBeatsPerMeasure: Int = 4,
    val customMeasureBeats: MutableSet<Int> = mutableSetOf(),
    var meterNote: MeterNote = MeterNote.QUARTER
  ) {
    enum class MeterNote { SIXTEENTH, EIGHTH, QUARTER, HALF, WHOLE }
  }

  override fun transpose(interval: Int) = Harmony(
    tonic,
    TreeMap(changes.mapValues { it.value.transpose(interval) }),
    subdivisionsPerBeat
  )
}