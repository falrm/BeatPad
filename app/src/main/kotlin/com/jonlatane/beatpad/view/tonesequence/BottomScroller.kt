package com.jonlatane.beatpad.view.tonesequence

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.view.ViewManager
import com.jonlatane.beatpad.view.NonDelayedHorizontalScrollView
import org.jetbrains.anko.custom.ankoView

/**
 * A [NonDelayedHorizontalScrollView] that tracks if the user is holding it down.
 */
class BottomScroller(
	context: Context,
	scrollingEnabled: Boolean = true
): NonDelayedHorizontalScrollView(context, scrollingEnabled) {
	val isHeldDown get() = activePointers.isNotEmpty()
	var onHeldDownChanged: ((Boolean) -> Unit)? = null
	private var activePointers: MutableSet<Int> = mutableSetOf()

	override fun onTouchEvent(ev: MotionEvent): Boolean {
		val initiallyHeldDown = isHeldDown
		// get pointer index from the event object
		val pointerIndex = ev.actionIndex
		// get pointer ID
		val pointerId = ev.getPointerId(pointerIndex)
		val maskedAction = ev.actionMasked
		when (maskedAction) {

			MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
				// We have a new pointer. Lets add it to the list of pointers
				val f = PointF()
				f.x = ev.getX(pointerIndex)
				f.y = ev.getY(pointerIndex)
				activePointers.add(pointerId)
			}
			MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
				activePointers.remove(pointerId)
			}
		}
		invalidate()
		if(isHeldDown != initiallyHeldDown) {
			onHeldDownChanged?.invoke(isHeldDown)
		}
		return true
	}

	override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
		return super.onInterceptTouchEvent(ev)
	}
}
