package com.jonlatane.beatpad.model

import java.util.*

/**
 * A [Pattern] can be thought of a sequence of changes, all at points in time expressible
 * by rational numbers (i.e. integer fractions, n/d). A Pattern is guaranteed to have at least
 * one change at all times.
 */
interface Pattern<T : Transposable<T>> : Transposable<Pattern<T>> {
  val changes: NavigableMap<Int, T>
  val subdivisionsPerBeat: Int
  val length: Int
  var tonic: Int

  fun changeBefore(position: Int) = changes[changes.floorKey(position) ?: changes.lastKey()]!!
  fun isChangeAt(position: Int) = changes.containsKey(position)
  fun isSustainAt(position: Int) = !isChangeAt(position)
}