package com.jonlatane.beatpad.view.melody

import android.graphics.PointF
import android.util.SparseArray
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.melody.RationalMelody
import java.util.*

/**
 * In handling Beat interactions,
 */
interface MelodyBeatEventHandlerBase {
  /**
   * A click in a beat could return any position
   */
  fun getPositionAndElement(x: Float): Pair<Int, Transposable<*>?>?
  val beatPosition: Int
  val downPointers: SparseArray<PointF>
	val melody: Melody<*>?
  val changes: NavigableMap<Int, out Transposable<*>>? get() = melody?.changes
}