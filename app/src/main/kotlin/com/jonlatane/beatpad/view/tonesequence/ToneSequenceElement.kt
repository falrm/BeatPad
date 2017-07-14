package com.jonlatane.beatpad.view.tonesequence

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.ViewManager
import collections.forEach
import com.jonlatane.beatpad.harmony.Rest
import com.jonlatane.beatpad.harmony.ToneSequence.Step
import com.jonlatane.beatpad.harmony.ToneSequence.Step.Note
import com.jonlatane.beatpad.harmony.ToneSequence.Step.Sustain
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.util.HideableView
import com.jonlatane.beatpad.view.melody.BaseMelodyView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.warn


class ToneSequenceElement @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : BaseMelodyView(context, attrs, defStyle), HideableView, AnkoLogger {
	init {
		showSteps = true
	}
	lateinit var viewModel: ToneSequenceViewModel

	// Lazy loaded
	val seqIndex: Int by lazy {
		viewModel.elements.indexOf(this)
	}
	var step: Step?
		get() = viewModel.toneSequence.steps[seqIndex]
		set(value) {
			if(value != null && isVisible)
				viewModel.toneSequence.steps[seqIndex] = value
		}
	val isVisible: Boolean get() = seqIndex < viewModel.toneSequence.steps.size
	val isDownbeat: Boolean get() = seqIndex % viewModel.toneSequence.stepsPerBeat == 0

	override var initialHeight: Int? = null
	override val renderVertically = true
	override val halfStepsOnScreen = 88
	override val drawPadding = 30
	override var chord: Chord
		get() = viewModel.topology.chord
		set(value) { throw UnsupportedOperationException() }

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		if(isVisible) {
			drawStepNotes(canvas)
		}
	}

	private val p = Paint()
	private fun drawStepNotes(canvas: Canvas) {
		p.color = when(step) {
			is Note -> 0xAA212121.toInt()
			is Sustain -> 0xAA424242.toInt()
			null -> 0x00FFFFFF
		}
		try {
			val tones = when (step) {
				is Note -> (step as Note).tones
				is Sustain -> (step as Sustain).note.tones
				null -> emptySet<Int>()
			}
			tones.forEach { tone ->
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
		} catch(t: Throwable) {
			warn("Error drawing pressed notes in sequence", t)
			invalidate()
		}
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
				if(step is Note) {
					val tones = (step as Note).tones
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


fun ViewManager.toneSequenceElement(theme: Int = 0)
	= toneSequenceElement(theme) {}

inline fun ViewManager.toneSequenceElement(theme: Int = 0, init: ToneSequenceElement.() -> Unit)
	= ankoView({ ToneSequenceElement(it) }, theme, init)
