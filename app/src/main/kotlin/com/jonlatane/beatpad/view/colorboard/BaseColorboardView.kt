package com.jonlatane.beatpad.view.colorboard

import android.app.Application
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.harmony.chord.Maj7
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.mod12
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip
import org.jetbrains.anko.withAlpha

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
	override fun color(resourceId: Int) = context.color(resourceId)

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		canvas.getClipBounds(bounds)
		canvas.drawColorGuide()
		canvas.renderSteps()
	}
}