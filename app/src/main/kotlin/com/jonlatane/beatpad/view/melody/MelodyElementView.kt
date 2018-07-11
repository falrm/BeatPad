package com.jonlatane.beatpad.view.melody

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import com.jonlatane.beatpad.model.Melody.Element.Note
import com.jonlatane.beatpad.model.Melody.Element.Sustain
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.util.HideableView
import com.jonlatane.beatpad.view.colorboard.AlphaDrawer
import com.jonlatane.beatpad.view.colorboard.BaseColorboardView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip
import org.jetbrains.anko.warn


class MelodyElementView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : BaseColorboardView(context, attrs, defStyle), EventArticulationHandler, EventEditingHandler, AnkoLogger {

	init {
		showSteps = true
	}
	lateinit var viewModel: MelodyViewModel

	override var elementPosition = 0
	override inline val melody get() = viewModel.openedMelody
	inline val nextElement get() = elements[elementPosition % elements.size]
	inline val isDownbeat: Boolean get() = elementPosition % melody.subdivisionsPerBeat == 0

	override val downPointers = SparseArray<PointF>()
	//override var initialHeight: Int? = null
	override val renderVertically = true
	override val halfStepsOnScreen = 88f
	override val drawPadding = dip(5)
	override val nonRootPadding = dip(5)
	override var chord: Chord
		get() = viewModel.orbifold.chord
		set(value) { throw UnsupportedOperationException() }
	override val melodyOffset get() = viewModel.openedMelody.offsetUnder(viewModel.orbifold.chord)

	override fun onDraw(canvas: Canvas) {
		colorGuideAlpha = if(viewModel.playbackPosition == elementPosition) 255 else 187
		super.onDraw(canvas)
		canvas.drawStepNotes()
		canvas.drawRhythm()
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		return when (viewModel.melodyEditingModifiers.modifier) {
			MelodyEditingModifiers.Modifier.None -> false
			MelodyEditingModifiers.Modifier.Editing -> onTouchEditEvent(event)
			MelodyEditingModifiers.Modifier.Articulating -> onTouchArticulateEvent(event)
			MelodyEditingModifiers.Modifier.Transposing -> true
		}
	}

	override fun getTone(y: Float): Int {
		return Math.round(lowestPitch + 88 * (height - y) / height)
	}

	private val p = Paint()
	private fun Canvas.drawStepNotes() {
		p.color = when(element) {
			is Note -> 0xAA212121.toInt()
			is Sustain -> 0xAA424242.toInt()
		}
		try {
			val tones = when (element) {
				is Note -> (element as Note).tones
				is Sustain -> (element as Sustain).note.tones
			}
			if(tones.isNotEmpty()) {
				val leftMargin = when(element) {
					is Note -> drawPadding
					is Sustain -> 0
				}
				val rightMargin = when(nextElement) {
					is Note -> drawPadding
					is Sustain -> 0
				}
				tones.forEach { tone ->
					val realTone = tone + melodyOffset
					val top = height - height * (realTone - AlphaDrawer.BOTTOM) / 88f
					val bottom = height - height * (realTone - AlphaDrawer.BOTTOM + 1) / 88f
					drawRect(
						bounds.left.toFloat() + leftMargin,
						top,
						bounds.right.toFloat() - rightMargin,
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
}
