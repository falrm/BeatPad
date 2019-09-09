package com.jonlatane.beatpad.view.colorboard

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.model.chord.Maj7
import com.jonlatane.beatpad.util.color
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip

abstract class BaseColorboardView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : View(context, attrs, defStyle), CanvasToneDrawer, ColorGuide, AnkoLogger {
	override var chord = Chord(0, Maj7)
	val drawnAlpha get() = (255 * alpha).toInt()
	override var colorGuideAlpha = 255
	override val drawnColorGuideAlpha get() = (colorGuideAlpha * alpha).toInt()
	override var showSteps = true
	override val drawPadding = 0
	override val axisLength get() = (if(renderVertically) height else width).toFloat()
	override var paint = Paint()
	override var bounds = Rect()
	override val drawingContext: Context get() = context
	override fun color(resourceId: Int) = context.color(resourceId)
	override fun dip(value: Float): Int = context.dip(value)
	override fun dip(value: Int): Int = context.dip(value)
}