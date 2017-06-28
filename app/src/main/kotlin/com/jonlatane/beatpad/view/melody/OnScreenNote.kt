package com.jonlatane.beatpad.view.melody

import com.jonlatane.beatpad.sensors.Orientation

internal data class OnScreenNote(
	var tone: Int = 0,
	var pressed: Boolean = false,
	var bottom: Float = 0f,
	var top: Float = 0f
)

internal fun BaseMelodyView.getOrCreateNote(
	tone: Int,
	pressed: Boolean,
	bottom: Float,
	top: Float,
	index: Int
) = onScreenNoteCache.getOrPut(index, { OnScreenNote() }).apply {
	this.tone = tone
	this.pressed = pressed
	this.bottom = bottom
	this.top = top
}

private val BaseMelodyView.axisLength get() = (if(renderVertically) height else width).toFloat()

internal val BaseMelodyView.onScreenNotes: List<OnScreenNote> get() {
	val result = mutableListOf<OnScreenNote>()
	val orientationRange = BaseMelodyView.TOP - BaseMelodyView.BOTTOM - halfStepsOnScreen
	val bottomMostPoint: Float = BaseMelodyView.BOTTOM + (Orientation.normalizedDevicePitch() * orientationRange)
	val bottomMostNote: Int = Math.floor(bottomMostPoint.toDouble()).toInt()
	var noteIndex = 0
	var currentScreenNote = getOrCreateNote(
		tone = chord.closestTone(bottomMostNote),
		pressed = false,
		bottom = 0f,
		top = (bottomMostNote - bottomMostPoint) * axisLength/ halfStepsOnScreen,
		index = noteIndex++
	)
	(bottomMostNote..(bottomMostNote + halfStepsOnScreen + 1)).forEach {
		toneMaybeNotInChord ->
		val toneInChord = chord.closestTone(toneMaybeNotInChord)
		if(toneInChord != currentScreenNote.tone) {
			result.add(currentScreenNote)
			currentScreenNote = getOrCreateNote(
				tone = toneInChord,
				pressed = false,
				bottom = currentScreenNote.top,
				top = currentScreenNote.top,
				index = noteIndex++
			)
		}
		currentScreenNote.top += axisLength / halfStepsOnScreen
	}
	result.add(currentScreenNote)
	return result
}