package com.jonlatane.beatpad.view.melody

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.View
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.Maj7
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.util.HideableView
import com.jonlatane.beatpad.util.mod12
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.concurrent.ConcurrentHashMap

class MelodyView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : View(context, attrs, defStyle), HideableView, AnkoLogger {
	val instrument = MIDIInstrument()
	var chord = Chord(0, Maj7)
	val tones get() = chord.getTones(BOTTOM, TOP)
	override var initialHeight: Int? = null
	internal val onScreenNoteCache = ConcurrentHashMap<Int, OnScreenNote>()
	private val density = context.resources.displayMetrics.density
	private var activePointers: SparseArray<PointF> = SparseArray()
	private var pointerTones = SparseIntArray()
	private var pointerVelocities = SparseIntArray()

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

	private val backgroundColor = color(R.color.colorAccent)
	private var paint = Paint()
	private var bounds = Rect()
	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		canvas.getClipBounds(bounds)
		for((tone, _, xMin, xMax) in onScreenNotes) {
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
			canvas.drawRect(
				xMin,
				bounds.top.toFloat(),
				xMax,
				bounds.bottom.toFloat(),
				paint
			)
		}
		paint.color = 0xCC000000.toInt()
		for (i in 0..activePointers.size() - 1) {
			val key = activePointers.keyAt(i)
			// get the object by the key.
			val pointer = activePointers[key]
			paint.alpha = pointerVelocities[key] * 2
			canvas.drawCircle(pointer.x, pointer.y, 25f * density, paint)
		}
		invalidate()
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
		return onScreenNotes.find { x in (it.xMin..it.xMax) }!!.tone
	}

	fun color(resId: Int) =
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			resources.getColor(resId, context.theme)
		else @Suppress("Deprecated")
			resources.getColor(resId)

	internal fun closestToneInChord(tone: Int): Int {
		return tones.minBy {
			Math.abs(tone - it)
		}!!
	}

	internal val halfStepsOnScreen = 15

	companion object {
		internal val BOTTOM = -60
		internal val TOP = 29
	}
}
