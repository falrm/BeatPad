package com.jonlatane.beatpad.view.colorboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
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
) : View(context, attrs, defStyle), AnkoLogger {
	open var chord = Chord(0, Maj7)
	open var backgroundAlpha = 255
	var showSteps = true
	open val drawPadding = 0
	open val nonRootPadding = dip(13f)
	abstract val renderVertically: Boolean
	val axisLength get() = (if(renderVertically) height else width).toFloat()
	abstract val halfStepsOnScreen: Int

	internal var paint = Paint()
	internal var bounds = Rect()

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		canvas.getClipBounds(bounds)
		canvas.drawToneMappedRegions()
		canvas.renderSteps()
	}

	fun Canvas.drawToneMappedRegions() {
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
			val extraPadding = if(tone.mod12 == chord.root) 0 else nonRootPadding
			if(renderVertically) {
				drawRect(
					bounds.left.toFloat() + drawPadding + extraPadding,
					bounds.height() - rectBottom,
					bounds.right.toFloat() - drawPadding - extraPadding,
					bounds.height() - rectTop, // backwards y-axis bullshittery
					paint
				)
			} else {
				drawRect(
					rectBottom,
					bounds.top.toFloat() + drawPadding + extraPadding,
					rectTop,
					bounds.bottom.toFloat() - drawPadding - extraPadding,
					paint
				)
			}
		}
	}

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

	internal val onScreenNotes: List<OnScreenNote> get() {
		val result = kotlin.collections.mutableListOf<OnScreenNote>()
		val orientationRange = com.jonlatane.beatpad.view.colorboard.BaseColorboardView.TOP - com.jonlatane.beatpad.view.colorboard.BaseColorboardView.BOTTOM + 1 - halfStepsOnScreen
		val bottomMostPoint: Float = com.jonlatane.beatpad.view.colorboard.BaseColorboardView.BOTTOM + (com.jonlatane.beatpad.sensors.Orientation.normalizedDevicePitch() * orientationRange)
		val bottomMostNote: Int = java.lang.Math.floor(bottomMostPoint.toDouble()).toInt()
		var currentScreenNote = com.jonlatane.beatpad.view.colorboard.OnScreenNote(
			tone = chord.closestTone(bottomMostNote),
			pressed = false,
			bottom = 0f,
			top = (bottomMostNote - bottomMostPoint) * axisLength / halfStepsOnScreen
		)
		for (toneMaybeNotInChord in (bottomMostNote..(bottomMostNote + halfStepsOnScreen + 1))) {
			val toneInChord = chord.closestTone(toneMaybeNotInChord)
			if (toneInChord != currentScreenNote.tone) {
				result.add(currentScreenNote)
				currentScreenNote = com.jonlatane.beatpad.view.colorboard.OnScreenNote(
					tone = toneInChord,
					pressed = false,
					bottom = currentScreenNote.top,
					top = currentScreenNote.top
				)
			}
			currentScreenNote.top += axisLength / halfStepsOnScreen
		}
		result.add(currentScreenNote)
		return result
	}

	companion object {
		val BOTTOM = -39
		val TOP = 48
	}
}