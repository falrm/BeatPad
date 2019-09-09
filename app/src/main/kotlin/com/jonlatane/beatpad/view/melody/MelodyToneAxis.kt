package com.jonlatane.beatpad.view.melody

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.jonlatane.beatpad.util.HideableView
import com.jonlatane.beatpad.view.colorboard.BaseColorboardView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip
import org.jetbrains.anko.warn

class MelodyToneAxis @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : BaseColorboardView(context, attrs, defStyle), HideableView, AnkoLogger {
	override var initialHeight: Int? = null
	override var initialWidth: Int? = null
	override var initialTopMargin: Int? = null
	override var initialBottomMargin: Int? = null
	override var initialLeftMargin: Int? = null
	override var initialRightMargin: Int? = null
	override val renderVertically = true
	override val halfStepsOnScreen = 88f
	override var colorGuideAlpha = 100
	override val drawPadding = dip(10)
	override val nonRootPadding = dip(5)

	init {
		showSteps = true
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		canvas.getClipBounds(bounds)
		//canvas.drawColorGuide()
		canvas.renderSteps()
		drawStepNotes(canvas)
	}

	private val p = Paint()
	private fun drawStepNotes(canvas: Canvas) {
		p.color = 0xFF000000.toInt()
		try {
			(-5..5).forEach { octave ->
				listOf(1, 3, 6, 8, 10).forEach { step ->
					val tone = 12 * octave + step
					val top = height - height * (tone - lowestPitch) / 88f
					val bottom = height - height * (tone - lowestPitch + 1) / 88f
					canvas.drawRect(
						bounds.left.toFloat(),
						top,
						bounds.right.toFloat(),
						bottom,
						p
					)
				}
			}
		} catch(t: Throwable) {
			warn("Error drawing pressed notes in sequence", t)
			invalidate()
		}
	}
}
