package com.jonlatane.beatpad.view.melody

import android.graphics.PointF
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Melody.Element
import com.jonlatane.beatpad.view.colorboard.AlphaDrawer

interface EventEditingHandler : BaseEventHandler, AlphaDrawer {
	val melodyOffset: Int // non-zero only when melody is not in fixed position mode
	fun getTone(y: Float): Int

	fun onTouchEditEvent(event: MotionEvent): Boolean {
		// get pointer index from the event object
		val pointerIndex = event.actionIndex
		// get pointer ID
		val pointerId = event.getPointerId(pointerIndex)
		val maskedAction = event.actionMasked
		when (maskedAction) {

			MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
				val tone = getTone(event.getY(pointerIndex))
				if(element is Element.Note) {
					val tones = (element as Element.Note).tones
					val targetTone = tone - melodyOffset
					if(!tones.remove(targetTone)) tones.add(targetTone)
				}
			}
			MotionEvent.ACTION_MOVE -> {}
			MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {}
		}
		(this as? View)?.invalidate()
		return true
	}
}