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
) : View(context, attrs, defStyle), ColorGuide, AnkoLogger {
	override var chord = Chord(0, Maj7)
	val drawnAlpha get() = (255 * alpha).toInt()
	override var colorGuideAlpha = 255
	override val drawnColorGuideAlpha get() = (colorGuideAlpha * alpha).toInt()
	var showSteps = true
	override val drawPadding = 0
	override val nonRootPadding = dip(13f)
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

	// Renders the dividers that separate A, A#, B, C, etc. visually to the user
	fun Canvas.renderSteps() {
		paint.color = context.color(R.color.colorPrimaryDark)
		if(showSteps) {
			val halfStepWidth: Float = axisLength / halfStepsOnScreen
			var linePosition = onScreenNotes.first().top - 12 * halfStepWidth //TODO gross hack
			while(linePosition < axisLength) {
				if(renderVertically) {
					drawLine(
						bounds.left.toFloat(),
						linePosition,
						bounds.right.toFloat(),
						linePosition,
						Paint()
					)
				} else {
					drawLine(
						linePosition,
						bounds.top.toFloat(),
						linePosition,
						bounds.bottom.toFloat(),
						paint
					)
				}
				linePosition += halfStepWidth
			}
		}
	}
}