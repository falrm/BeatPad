package com.jonlatane.beatpad.view.melody

import android.graphics.PointF
import android.util.SparseArray
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.melody.RationalMelody
import java.util.*

interface BaseMelodyEventHandler {
	val downPointers: SparseArray<PointF>
	val melody: Melody<*>?
	val elementPosition: Int
	val changes: NavigableMap<Int, out Transposable<*>> get() = melody?.changes ?: TreeMap()
	val isChange: Boolean get() = melody?.isChangeAt(elementPosition) ?: false
  val change: Transposable<*>? get() = melody?.changeBefore(elementPosition)
	var element: Transposable<*>?
    get() = changes[elementPosition % (melody?.length ?: 1)]
    set(value) {
      when(value) {
        is RationalMelody.Element -> (melody as? RationalMelody)?.apply {
          changes[elementPosition] = value
        }
      }
    }
}