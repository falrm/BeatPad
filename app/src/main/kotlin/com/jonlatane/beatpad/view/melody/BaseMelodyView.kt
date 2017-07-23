package com.jonlatane.beatpad.view.melody

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.Maj7
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.mod12
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.withAlpha
import java.util.concurrent.ConcurrentHashMap

abstract class BaseMelodyView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : View(context, attrs, defStyle), AnkoLogger {
	open var chord = Chord(0, Maj7)
	open var backgroundAlpha = 255
	var showSteps = true
	open val drawPadding = 0
	abstract val renderVertically: Boolean
	val axisLength get() = (if(renderVertically) height else width).toFloat()
	abstract val halfStepsOnScreen: Int

	internal var paint = Paint()
	internal var bounds = Rect()

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		canvas.getClipBounds(bounds)
		for((tone, _, rectBottom, rectTop) in onScreenNotes) {
			paint.color = when((tone - chord.root).mod12) {
				0 -> color(R.color.tonic)
				1 -> color(R.color.flatTwo)
				2 -> color(R.color.two)
				3 -> color(R.color.flatThree)
				4 -> color(R.color.three)
				5 -> color(R.color.four)
				6 -> color(R.color.flatFive)
				7 -> color(R.color.five)
				8 -> color(R.color.sharpFive)
				9 -> color(R.color.six)
				10 -> color(R.color.flatSeven)
				11 -> color(R.color.seven)
				else -> throw IllegalStateException()
			}.withAlpha(backgroundAlpha)
			if(renderVertically) {
				canvas.drawRect(
					bounds.left.toFloat() + drawPadding,
					bounds.height() - rectBottom,
					bounds.right.toFloat() - drawPadding,
					bounds.height() - rectTop, // backwards y-axis bullshittery
					paint
				)
			} else {
				canvas.drawRect(
					rectBottom,
					bounds.top.toFloat() - drawPadding,
					rectTop,
					bounds.bottom.toFloat() + drawPadding,
					paint
				)
			}
		}
		renderSteps(canvas)
	}

	private fun renderSteps(canvas: Canvas) {
		paint.color = R.color.colorPrimaryDark
		if(showSteps) {
			val halfStepWidth: Float = axisLength / halfStepsOnScreen
			var linePosition = onScreenNotes.first().top - 12 * halfStepWidth //TODO gross hack
			while(linePosition < axisLength) {
				if(renderVertically) {
					canvas.drawLine(
						bounds.left.toFloat(),
						linePosition,
						bounds.right.toFloat(),
						linePosition,
						Paint()
					)
				} else {
					canvas.drawLine(
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

	companion object {
		val BOTTOM = -39
		val TOP = 48
	}
}