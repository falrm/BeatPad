package com.jonlatane.beatpad.view.colorboard

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1

interface AlphaDrawer {
	val paint: Paint
	/**
	 * Represent the bounds for whatever is to be drawn
	 */
	val bounds: Rect
	val renderVertically: Boolean
	val axisLength: Float
	val halfStepsOnScreen: Float
	val highestPitch get() = TOP // Top C on an 88-key piano
	val lowestPitch get() = BOTTOM // Bottom A, ditto
	val drawingContext: Context
	val halfStepWidth: Float get() = axisLength / halfStepsOnScreen
	fun color(resourceId: Int): Int

	fun <T> Paint.preserveColor(block: () -> T): T {
		val initialColor = color
		val result = block()
		color = initialColor
		return result
	}

	companion object {
		const val BOTTOM = -39
		const val TOP = 48
	}
}