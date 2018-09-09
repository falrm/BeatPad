package com.jonlatane.beatpad.view.melody

import android.graphics.PointF
import android.util.SparseArray
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.melody.RationalMelody
import java.util.*

interface BaseMelodyEventHandler {
	fun getElement(x: Float): Transposable<*>?
	val downPointers: SparseArray<PointF>
	val melody: Melody<*>?
  val changes: NavigableMap<Int, out Transposable<*>>? get() = melody?.changes
}