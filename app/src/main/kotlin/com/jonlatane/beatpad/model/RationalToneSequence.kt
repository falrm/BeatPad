package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.util.mod12
import com.jonlatane.beatpad.model.ToneSequence.Subdivision

class RationalToneSequence(
  subdivisions: List<Subdivision> = emptyList(),
  /** A value of 4 would indicate sixteenth notes in 4/4 time */
  override var subdivisionsPerBeat: Int = 1
): ToneSequence {
  override val subdivisions: MutableList<Subdivision>
    = subdivisions.toMutableList()
  override var relativeTo: Int = 0
}