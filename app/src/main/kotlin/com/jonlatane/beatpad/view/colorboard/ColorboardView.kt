package com.jonlatane.beatpad.view.colorboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.util.AttributeSet
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.MotionEvent
import com.jonlatane.beatpad.model.Instrument
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.util.HideableView
import org.jetbrains.anko.info
import kotlin.properties.Delegates.observable

class ColorboardView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : BaseColorboardView(context, attrs, defStyle), HideableView {
	override var initialHeight: Int? = null
	var instrument by observable<Instrument>(MIDIInstrument()) { _, old, _ -> old.stop() }
	private val density = context.resources.displayMetrics.density
	private var activePointers: SparseArray<PointF> = SparseArray()
	private var pointerTones = SparseIntArray()
	private var pointerVelocities = SparseIntArray()
	override val renderVertically = false
	override val halfStepsOnScreen = 15

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		paint.color = 0xCC000000.toInt()
		for (i in 0..activePointers.size() - 1) {
			val key = activePointers.keyAt(i)
			// get the object by the key.
			val pointer = activePointers[key]
			paint.alpha = pointerVelocities[key] * 2
			canvas.drawCircle(pointer.x, pointer.y, 25f * density, paint)
		}
		post {
			Thread.sleep(10L)
			invalidate()
		}
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		// get pointer index from the event object
		val pointerIndex = event.actionIndex
		// get pointer ID
		val pointerId = event.getPointerId(pointerIndex)
		val maskedAction = event.actionMasked
		when (maskedAction) {

			MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
				// We have a new pointer. Lets add it to the list of pointers
				val f = PointF()
				f.x = event.getX(pointerIndex)
				f.y = event.getY(pointerIndex)
				activePointers.put(pointerId, f)

				val tone = getTone(f.x)
				info("tone: $tone")
				val velocity = getVelocity(f.y)
				pointerTones.put(pointerId, tone)
				pointerVelocities.put(pointerId, velocity)
				info("playing $tone with velocity $velocity")
				instrument.play(tone, velocity)
			}
			MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
				instrument.stop(pointerTones.get(pointerId))
				activePointers.remove(pointerId)
			}
		}
		invalidate()
		return true
	}


	private fun getVelocity(y: Float): Int {
		var velocity01: Float = (height - y) / height
		velocity01 = Math.sqrt(Math.sqrt(Math.max(0.1f, velocity01).toDouble())).toFloat()
		val velocity: Int = Math.min(127, Math.max(10, Math.round(
			velocity01 * 127
		)))
		return velocity
	}

	private fun getTone(x: Float): Int {
		return onScreenNotes.find { x in (it.bottom..it.top) }!!.tone
	}
}
