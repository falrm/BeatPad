package com.jonlatane.beatpad.view.melody

interface MelodyBeatEventArticulationHandler : MelodyBeatEventHandlerBase {
/*
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
				if(isChange) {
					(element as Melody.Element.Note)
					true
				} else {
					(element as Melody.Element.Sustain).note
					true
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

	private fun getArticulatoryNeighbors(): Map<Int, MelodyBeatView> {
		TODO()
	}

	/**
	 * If [element] is a Rest (con
	 */
	fun getElementArticulation(): Int {
		var lookBehinds = 0
		while(
			elements[(beatPosition - lookBehinds)] is Melody.Element.Sustain || false

		) {
			lookBehinds++
		}
		var duration = 1
		while(elements[beatPosition + duration] is Melody.Element.Sustain) {
			duration += 1
		}
		return duration + lookBehinds
	}

	fun setElementArticulation(value: Int) {
		var lookBehinds = 0
		while(elements[(beatPosition - lookBehinds)] is Melody.Element.Sustain) {
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
	*/
}