package com.jonlatane.beatpad.model

import java.util.*
import kotlin.NoSuchElementException

/**
 * A [Pattern] can be thought of a sequence of changes, all at points in time expressible
 * by rational numbers (i.e. integer fractions, n/d). A Pattern is guaranteed to have at least
 * one change at all times. The actual data structure of a Pattern is designed to lend itself
 * to on-screen rendering and manipulation. While we might expect [Pattern.changes] to be a
 * mapping from some "rational number" type to our [Transposable] changes, using [Int] indexes
 * and the value [subdivisionsPerBeat] is much more practical for interfacing with iOS, Android
 * and JavaScript APIs for rendering large collections efficiently.
 */
interface Pattern<T : Transposable<T>> : Transposable<Pattern<T>> {
  val changes: NavigableMap<Int, T>
  val subdivisionsPerBeat: Int
  val length: Int
  var tonic: Int

  fun beat(beatPosition: Int): NavigableMap<Int, T> = changes.subMap(
    beatPosition * subdivisionsPerBeat, true,
    (beatPosition + 1) * subdivisionsPerBeat, false
  )

  fun higherKey(position: Int) = changes.higherKey(position) ?: changes.firstKey()
  fun ceilingKey(position: Int) = changes.ceilingKey(position) ?: changes.firstKey()
  fun floorKey(position: Int) = changes.floorKey(position) ?: changes.lastKey()
  fun lowerKey(position:Int) = changes.lowerKey(position) ?: changes.lastKey()

  fun changeBefore(position: Int) = changes[floorKey(position)]!!
  fun isChangeAt(position: Int) = changes.containsKey(position)
  fun isSustainAt(position: Int) = !isChangeAt(position)
}