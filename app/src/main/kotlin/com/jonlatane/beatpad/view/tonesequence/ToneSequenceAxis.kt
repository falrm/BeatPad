package com.jonlatane.beatpad.view.tonesequence

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.ViewManager
import com.jonlatane.beatpad.util.HideableView
import com.jonlatane.beatpad.view.melody.BaseMelodyView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.warn

class ToneSequenceAxis @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : BaseMelodyView(context, attrs, defStyle), HideableView, AnkoLogger {
	override var initialHeight: Int? = 1
	override val renderVertically = true
	override val halfStepsOnScreen = 88
	override var backgroundAlpha = 100

	init {
		showSteps = true
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		drawStepNotes(canvas)
	}

	private val p = Paint()
	private fun drawStepNotes(canvas: Canvas) {
		p.color = 0xFF000000.toInt()
		try {
			(-5..5).forEach { octave ->
				listOf(1, 3, 6, 8, 10).forEach { step ->
					val tone = 12 * octave + step
					val top = height - height * (tone - BaseMelodyView.BOTTOM) / 88f
					val bottom = height - height * (tone - BaseMelodyView.BOTTOM + 1) / 88f
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


fun ViewManager.toneSequenceAxis(theme: Int = 0)
	= toneSequenceAxis(theme) {}

inline fun ViewManager.toneSequenceAxis(theme: Int = 0, init: ToneSequenceAxis.() -> Unit)
	= ankoView({ ToneSequenceAxis(it) }, theme, init)
