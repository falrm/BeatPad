package com.jonlatane.beatpad.view.beat

import android.graphics.PointF
import android.util.SparseArray
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Pattern
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.melody.RationalMelody
import java.util.*

/**
 * In handling Beat interactions,
 */
interface BeatEventHandlerBase<ElementType: Transposable<ElementType>, PatternType: Pattern<ElementType>> {
  /**
   * A click in a beat could return any position
   */
  fun getPositionAndElement(x: Float): Pair<Int, Transposable<*>?>?
  val beatPosition: Int
  val downPointers: SparseArray<PointF>
  val pattern: PatternType?
  val changes: NavigableMap<Int, out ElementType>? get() = pattern?.changes
}