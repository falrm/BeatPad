package com.jonlatane.beatpad.view.melody

import android.view.MotionEvent
import android.view.View
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.util.vibrate
import com.jonlatane.beatpad.view.colorboard.AlphaDrawer

interface MelodyBeatEventEditingHandler : MelodyBeatEventHandlerBase, AlphaDrawer {

	fun melodyOffsetAt(elementPosition: Int): Int // non-zero only when melody is not in fixed position mode
	fun getTone(y: Float): Int

	fun onTouchEditEvent(event: MotionEvent): Boolean {
		// get pointer index from the event object
		val pointerIndex = event.actionIndex
		// get pointer ID
		val pointerId = event.getPointerId(pointerIndex)
		val maskedAction = event.actionMasked
		when (maskedAction) {

			MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
				getPositionAndElement(event.getX(pointerIndex))?.let { (position, element) ->
					val isChange = element != null
					val tone = getTone(event.getY(pointerIndex))
					if(isChange && element is RationalMelody.Element) {
            val targetTone = tone - melodyOffsetAt(position)
						when(displayType) {
							MelodyViewModel.DisplayType.COLORBLOCK -> {
								val tones = element.tones
								if(!tones.remove(targetTone)) tones.add(targetTone)
								vibrate(MelodyEditingModifiers.vibrationMs)
							}
							MelodyViewModel.DisplayType.NOTATION   -> {
                val playbackToneMap: Map<Int, List<Int>> = element.tones.groupBy {
                  melody!!.playbackToneUnder(it, chordAt(position)!!)
                }
                val targetPlaybackTone = melody!!.playbackToneUnder(targetTone, chordAt(position)!!)
                if(playbackToneMap.containsKey(targetPlaybackTone)) {
                  element.tones.removeAll(playbackToneMap[targetPlaybackTone].orEmpty())
                } else {
                  element.tones.add(targetPlaybackTone - melodyOffsetAt(position))
                }
							}
						}
					}
				}
			}
			MotionEvent.ACTION_MOVE -> {}
			MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {}
		}
		(this as? View)?.invalidate()
		return true
	}
}