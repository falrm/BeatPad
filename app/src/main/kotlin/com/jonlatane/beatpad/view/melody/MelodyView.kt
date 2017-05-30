package com.jonlatane.beatpad.view.melody

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.os.Build
import android.support.v4.graphics.ColorUtils
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.View
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.sensors.Orientation.normalizedDevicePitch

/**
 * Created by jonlatane on 5/6/17.
 */
class MelodyView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : View(context, attrs, defStyle) {
    val instrument = MIDIInstrument()
    var tones = listOf<Int>()
    internal val density = context.resources.displayMetrics.density
    internal var activePointers: SparseArray<PointF> = SparseArray()
    internal var pointerTones = SparseIntArray()
    internal var pointerVelocities = SparseIntArray()

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
                var tone = pointerId % 88 - 24
                if (!tones.isEmpty()) {
                    val screenWidth = tones.size / 4f
                    Log.i(TAG, "x=" + f.x + ",width=" + width)
                    val basePitch = normalizedDevicePitch() * (tones.size - screenWidth) + screenWidth * f.x / width
                    tone = tones[Math.max(0, Math.min(tones.size - 1, Math.round(basePitch)))]
                }
                var velocity01 = (height - f.y) / height

                //velocity01 = 0.5f + (float)Math.cbrt(velocity01 - 0.5f);
                velocity01 = Math.sqrt(Math.sqrt(Math.max(0.1f, velocity01).toDouble())).toFloat()
                val velocity = Math.min(127, Math.max(10, Math.round(
                        velocity01 * 127
                )))
                pointerTones.put(pointerId, tone)
                pointerVelocities.put(pointerId, velocity)
                Log.i(TAG, "playing $tone with velocity $velocity")
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

    @Suppress("DEPRECATION")
    internal val backgroundColor
            = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                resources.getColor(R.color.colorAccent, context.theme)
            else
                resources.getColor(R.color.colorAccent)
    internal val hsl = FloatArray(3)
    internal var paint = Paint()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = 0xCCFFFFFF.toInt()
        ColorUtils.colorToHSL(backgroundColor, hsl)
        hsl[1] = Orientation.normalizedDevicePitch()
        canvas.drawColor(ColorUtils.HSLToColor(hsl))
        for (i in 0..activePointers.size() - 1) {
            val key = activePointers.keyAt(i)
            // get the object by the key.
            val pointer = activePointers[key]
            paint.alpha = pointerVelocities[key]* 2
            canvas.drawCircle(pointer.x, pointer.y, 25f * density, paint)
        }
        invalidate()
    }

    companion object {
        private val TAG = MelodyView::class.simpleName
    }
}
