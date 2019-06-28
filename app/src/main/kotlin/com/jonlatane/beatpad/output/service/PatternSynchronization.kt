package com.jonlatane.beatpad.output.service

import com.jonlatane.beatpad.model.Pattern
import kotlin.math.floor

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
  // In the context of the "from" melody, in, say, sixteenth notes (subdivisionsPerBeat=4),
  // if this is 5, then currentBeat is 1.25.
  val fromBeat: Double = this.toDouble() / fromSubdivisionsPerBeat

  val toLengthBeats: Double = toLength.toDouble() / toSubdivisionsPerBeat
  val positionInToPattern: Double = fromBeat % toLengthBeats

  // This candidate for attack is the closest element index to the current tick
  val result = floor(positionInToPattern * toSubdivisionsPerBeat).toInt()
  return result
}

fun <T, U, R> Pair<T?, U?>.let(body: (T, U) -> R): R? {
  val t = first
  val u = second
  if (t != null && u != null) {
    return body(t, u)
  }
  return null
}

fun <S, T, U, R> Pair<Pair<S?, T?>, U?>.let(body: (S, T, U) -> R): R? {
  val s: S? = this.first.first
  val t: T? = first.second
  val u: U? = second
  if (s != null && t != null && u != null) {
    return body(s, t, u)
  }
  return null
}
