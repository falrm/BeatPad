package com.jonlatane.beatpad.view.colorboard

import android.graphics.Paint
import android.graphics.Rect

interface AlphaDrawer {
	val paint: Paint
	val bounds: Rect
	val renderVertically: Boolean
	val axisLength: Float
	val halfStepsOnScreen: Float
	val highestPitch get() = 48 // Top C on an 88-key piano
	val lowestPitch get() = -39 // Bottom A, ditto
	fun color(resourceId: Int): Int

	companion object {
		const val BOTTOM = -39
		const val TOP = 48
	}
}