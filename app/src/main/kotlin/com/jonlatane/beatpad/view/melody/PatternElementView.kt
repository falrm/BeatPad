package com.jonlatane.beatpad.view.melody

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import com.jonlatane.beatpad.model.Melody.Element
import com.jonlatane.beatpad.model.Melody.Element.Note
import com.jonlatane.beatpad.model.Melody.Element.Sustain
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.util.HideableView
import com.jonlatane.beatpad.view.colorboard.BaseColorboardView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn


class PatternElementView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : BaseColorboardView(context, attrs, defStyle), HideableView, AnkoLogger {
	init {
		showSteps = true
	}
	lateinit var viewModel: PatternViewModel

	var elementPosition = 0
	val element: Element get() = viewModel.toneSequence.elements[elementPosition]
	val isDownbeat: Boolean get() = elementPosition % viewModel.toneSequence.subdivisionsPerBeat == 0

	override var initialHeight: Int? = null
	override val renderVertically = true
	override val halfStepsOnScreen = 88
	override val drawPadding = 30
	override val nonRootPadding = 20
	override var chord: Chord
		get() = viewModel.orbifold.chord
		set(value) { throw UnsupportedOperationException() }

	override fun onDraw(canvas: Canvas) {
		backgroundAlpha = if(viewModel.playbackPosition == elementPosition) 255 else 187
		super.onDraw(canvas)
		canvas.drawStepNotes()
		canvas.drawRhythm()
	}

	private val p = Paint()
	private fun Canvas.drawStepNotes() {
		p.color = when(element) {
			is Note -> 0xAA212121.toInt()
			is Sustain -> 0xAA424242.toInt()
			null -> 0x00FFFFFF
		}
		try {
			val tones = when (element) {
				is Note -> (element as Note).tones
				is Sustain -> (element as Sustain).note.tones
				null -> emptySet<Int>()
			}
			tones.forEach { tone ->
				val top = height - height * (tone - BaseColorboardView.BOTTOM) / 88f
				val bottom = height - height * (tone - BaseColorboardView.BOTTOM + 1) / 88f
				drawRect(
					bounds.left.toFloat(),
					top,
					bounds.right.toFloat(),
					bottom,
					p
				)
			}
		} catch(t: Throwable) {
			warn("Error drawing pressed notes in sequence", t)
			invalidate()
		}
	}

	private fun Canvas.drawRhythm() {
		p.color = 0xAA212121.toInt()
		drawRect(
			bounds.left.toFloat(),
			bounds.top.toFloat(),
			bounds.left.toFloat() + if(isDownbeat) 10f else 5f,
			bounds.bottom.toFloat(),
			p
		)
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		if(!viewModel.bottomScroller.isHeldDown) return false
		// get pointer index from the event object
		val pointerIndex = event.actionIndex
		// get pointer ID
		val pointerId = event.getPointerId(pointerIndex)
		val maskedAction = event.actionMasked
		when (maskedAction) {

			MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
				val tone = getTone(event.getY(pointerIndex))
				if(element is Note) {
					val tones = (element as Note).tones
					if(!tones.remove(tone)) tones.add(tone)
				}
			}
			MotionEvent.ACTION_MOVE -> {}
			MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {}
		}
		invalidate()
		return true
	}

	private fun getTone(y: Float): Int {
		return Math.round(-39 + 88 * (height - y) / height)
	}
}
