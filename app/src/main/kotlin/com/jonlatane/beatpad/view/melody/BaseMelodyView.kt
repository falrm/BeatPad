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
import java.util.concurrent.ConcurrentHashMap

abstract class BaseMelodyView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : View(context, attrs, defStyle), AnkoLogger {
	var chord = Chord(0, Maj7)
	open val drawPadding = 0
	abstract val renderVertically: Boolean
	abstract val halfStepsOnScreen: Int

	internal val onScreenNoteCache = ConcurrentHashMap<Int, OnScreenNote>()

	internal var paint = Paint()
	private var bounds = Rect()

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
			}
			if(renderVertically) {
				canvas.drawRect(
					bounds.left.toFloat() + drawPadding,
					rectTop,
					bounds.right.toFloat() - drawPadding,
					rectBottom,
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
	}

	companion object {
		internal val BOTTOM = -60
		internal val TOP = 28
	}
}