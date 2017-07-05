package com.jonlatane.beatpad.view.melody

import com.jonlatane.beatpad.sensors.Orientation
import org.jetbrains.anko.info

internal data class OnScreenNote(
	var tone: Int = 0,
	var pressed: Boolean = false,
	var bottom: Float = 0f,
	var top: Float = 0f
)

internal val BaseMelodyView.onScreenNotes: List<OnScreenNote> get() {
	val result = mutableListOf<OnScreenNote>()
	val orientationRange = BaseMelodyView.TOP - BaseMelodyView.BOTTOM + 1 - halfStepsOnScreen
	val bottomMostPoint: Float = BaseMelodyView.BOTTOM + (Orientation.normalizedDevicePitch() * orientationRange)
	val bottomMostNote: Int = Math.floor(bottomMostPoint.toDouble()).toInt()
	var currentScreenNote = OnScreenNote(
		tone = chord.closestTone(bottomMostNote),
		pressed = false,
		bottom = 0f,
		top = (bottomMostNote - bottomMostPoint) * axisLength/ halfStepsOnScreen
	)
	for(toneMaybeNotInChord in (bottomMostNote..(bottomMostNote + halfStepsOnScreen + 1))) {
		val toneInChord = chord.closestTone(toneMaybeNotInChord)
		if(toneInChord != currentScreenNote.tone) {
			result.add(currentScreenNote)
			currentScreenNote = OnScreenNote(
				tone = toneInChord,
				pressed = false,
				bottom = currentScreenNote.top,
				top = currentScreenNote.top
			)
		}
		currentScreenNote.top += axisLength / halfStepsOnScreen
	}
	result.add(currentScreenNote)
	return result
}