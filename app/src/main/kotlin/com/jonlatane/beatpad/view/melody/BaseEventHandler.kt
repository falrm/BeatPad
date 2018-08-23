package com.jonlatane.beatpad.view.melody

import android.graphics.PointF
import android.util.SparseArray
import com.jonlatane.beatpad.model.Melody

interface BaseEventHandler {
	val downPointers: SparseArray<PointF>
	val melody: Melody
	val elementPosition: Int
	val elements get() = melody.elements
	val element get() = elements[elementPosition % elements.size]
}