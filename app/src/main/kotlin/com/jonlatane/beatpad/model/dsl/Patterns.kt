package com.jonlatane.beatpad.model.dsl

import com.jonlatane.beatpad.model.Pattern
import kotlin.math.floor

interface Patterns {
  companion object: Patterns

  fun Int.convertPatternIndex(
    from: Pattern<*>,
    to: Pattern<*>
  ): Int = convertPatternIndex(from.subdivisionsPerBeat, to)

  fun Int.convertPatternIndex(
    from: Int,
    to: Pattern<*>
  ): Int  = convertPatternIndex(
    fromSubdivisionsPerBeat = from,
    toSubdivisionsPerBeat = to.subdivisionsPerBeat,
    toLength = to.length
  )

  fun Int.convertPatternIndex(
    fromSubdivisionsPerBeat: Int,
    toSubdivisionsPerBeat: Int,
    toLength: Int = Int.MAX_VALUE
  ): Int {
    // In the storageContext of the "from" melody, in, say, sixteenth notes (subdivisionsPerBeat=4),
    // if this is 5, then currentBeat is 1.25.
    val fromBeat: Double = this.toDouble() / fromSubdivisionsPerBeat

    val toLengthBeats: Double = toLength.toDouble() / toSubdivisionsPerBeat
    val positionInToPattern: Double = fromBeat % toLengthBeats

    // This candidate for attack is the closest element index to the current tick
    val result = floor(positionInToPattern * toSubdivisionsPerBeat).toInt()
    return result
  }
}