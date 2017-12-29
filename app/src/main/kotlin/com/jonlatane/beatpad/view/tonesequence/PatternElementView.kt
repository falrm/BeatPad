package com.jonlatane.beatpad.view.tonesequence

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import com.jonlatane.beatpad.model.Pattern.Subdivision
import com.jonlatane.beatpad.model.Pattern.Subdivision.Note
import com.jonlatane.beatpad.model.Pattern.Subdivision.Sustain
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.util.HideableView
import com.jonlatane.beatpad.view.melody.BaseMelodyView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn


class PatternElementView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : BaseMelodyView(context, attrs, defStyle), HideableView, AnkoLogger {
	init {
		showSteps = true
	}
	lateinit var viewModel: PatternViewModel

	// Lazy loaded
	var seqIndex = 0
	var subdivision: Subdivision?
		get() = viewModel.toneSequence.subdivisions[seqIndex]
		set(value) {
			if(value != null && isVisible)
				viewModel.toneSequence.subdivisions[seqIndex] = value
		}
	val isVisible: Boolean get() = seqIndex < viewModel.toneSequence.subdivisions.size
	val isDownbeat: Boolean get() = seqIndex % viewModel.toneSequence.subdivisionsPerBeat == 0

	override var initialHeight: Int? = null
	override val renderVertically = true
	override val halfStepsOnScreen = 88
	override val drawPadding = 30
	override val nonRootPadding = 20
	override var chord: Chord
		get() = viewModel.orbifold.chord
		set(value) { throw UnsupportedOperationException() }

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		if(isVisible) {
			canvas.drawStepNotes()
			canvas.drawRhythm()
		}
	}

	private val p = Paint()
	private fun Canvas.drawStepNotes() {
		p.color = when(subdivision) {
			is Note -> 0xAA212121.toInt()
			is Sustain -> 0xAA424242.toInt()
			null -> 0x00FFFFFF
		}
		try {
			val tones = when (subdivision) {
				is Note -> (subdivision as Note).tones
				is Sustain -> (subdivision as Sustain).note.tones
				null -> emptySet<Int>()
			}
			tones.forEach { tone ->
				val top = height - height * (tone - BaseMelodyView.BOTTOM) / 88f
				val bottom = height - height * (tone - BaseMelodyView.BOTTOM + 1) / 88f
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
				if(subdivision is Note) {
					val tones = (subdivision as Note).tones
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
