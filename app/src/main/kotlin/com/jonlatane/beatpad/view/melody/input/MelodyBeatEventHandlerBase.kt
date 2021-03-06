package com.jonlatane.beatpad.view.melody.input

import android.graphics.PointF
import android.util.SparseArray
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.dsl.Patterns
import com.jonlatane.beatpad.util.vibrate
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.melody.toolbar.MelodyEditingModifiers
import org.jetbrains.anko.AnkoLogger
import java.util.*

/**
 * In handling Beat interactions,
 */
interface MelodyBeatEventHandlerBase: Patterns, AnkoLogger {
  /**
   * For a touch X value (time laid along the x-axis), returns the
   * element position (within the melody) and the element data there.
   */
  val displayType: MelodyViewModel.DisplayType
  fun getPositionAndElement(x: Float): Pair<Int, Transposable<*>?>?
  val beatPosition: Int
  val downPointers: SparseArray<PointF>
	val melody: Melody<*>?
  val harmony: Harmony
  val changes: NavigableMap<Int, out Transposable<*>>? get() = melody?.changes
  fun chordAt(elementPosition: Int, melody: Melody<*>) =
    harmony.let { harmony ->
      val harmonyPosition = elementPosition.convertPatternIndex(melody, harmony)
      val result = harmony.changeBefore(harmonyPosition)
      //info("Chord at $elementPosition is $result")
      result
    }

  fun updateMelodyDisplay()

  fun rejectionVibrate() {

    vibrate(MelodyEditingModifiers.vibrationMs)
  }
}