package com.jonlatane.beatpad.view.melody

import android.graphics.PointF
import android.util.SparseArray
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.output.service.convertPatternIndex
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.*

/**
 * In handling Beat interactions,
 */
interface MelodyBeatEventHandlerBase: AnkoLogger {
  /**
   * For a touch X value (time laid along the x-axis), returns the
   * element position (within the melody) and the element data there.
   */
  fun getPositionAndElement(x: Float): Pair<Int, Transposable<*>?>?
  val beatPosition: Int
  val downPointers: SparseArray<PointF>
	val melody: Melody<*>?
  val harmony: Harmony?
  val changes: NavigableMap<Int, out Transposable<*>>? get() = melody?.changes
  fun chordAt(elementPosition: Int) = melody?.let { melody ->
    harmony?.let { harmony ->
      val harmonyPosition = elementPosition.convertPatternIndex(melody, harmony)
      val result =harmony.changeBefore(harmonyPosition)
      //info("Chord at $elementPosition is $result")
      result
    }
  }
}