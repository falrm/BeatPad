package com.jonlatane.beatpad.view.melody

import android.graphics.PointF
import android.view.MotionEvent
import com.jonlatane.beatpad.model.Melody

interface EventArticulationHandler : BaseEventHandler {

	fun onTouchArticulateEvent(event: MotionEvent): Boolean {
		// get pointer index from the event object
		val pointerIndex = event.actionIndex
		// get pointer ID
		val pointerId = event.getPointerId(pointerIndex)
		val maskedAction = event.actionMasked
		return when (maskedAction) {
			MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
				val f = PointF()
				f.x = event.getX(pointerIndex)
				f.y = event.getY(pointerIndex)
				downPointers.put(pointerId, f)
				when (element) {
					is Melody.Element.Note -> {
						(element as Melody.Element.Note)
						true
					}
					is Melody.Element.Sustain -> {
						(element as Melody.Element.Sustain).note
						true
					}
				}
			}

			MotionEvent.ACTION_MOVE -> {
				val xDistance = event.getX(pointerIndex) - downPointers[pointerIndex].x
				//val nearest =

				true
			}
			MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
				downPointers.remove(pointerId)
				true
			}
			else -> true
		}
	}

	private fun getArticulatoryNeighbors(): Map<Int, MelodyElementView> {
		TODO()
	}

	/**
	 * If [element] is a Rest (con
	 */
	fun getElementArticulation(): Int {
		var lookBehinds = 0
		while(
			elements[(elementPosition - lookBehinds)] is Melody.Element.Sustain || false

		) {
			lookBehinds++
		}
		var duration = 1
		while(elements[elementPosition + duration] is Melody.Element.Sustain) {
			duration += 1
		}
		return duration + lookBehinds
	}

	fun setElementArticulation(value: Int) {
		var lookBehinds = 0
		while(elements[(elementPosition - lookBehinds)] is Melody.Element.Sustain) {
			lookBehinds++
		}
		var duration = 1
		while(
			lookBehinds + duration < value &&
			elements != null
		) {
			duration++
		}
	}
}