package com.jonlatane.beatpad.view.melody

import com.jonlatane.beatpad.sensors.Orientation

internal data class OnScreenNote(
	var tone: Int = 0,
	var pressed: Boolean = false,
	var xMin: Float = 0f,
	var xMax: Float = 0f
)

internal fun MelodyView.getOrCreateNote(
	tone: Int,
	pressed: Boolean,
	xMin: Float,
	xMax: Float,
	index: Int
) = onScreenNoteCache.getOrPut(index, { OnScreenNote() }).apply {
	this.tone = tone
	this.pressed = pressed
	this.xMin = xMin
	this.xMax = xMax
}

internal val MelodyView.onScreenNotes: List<OnScreenNote> get() {
	val result = mutableListOf<OnScreenNote>()
	val orientationRange = MelodyView.TOP - MelodyView.BOTTOM - halfStepsOnScreen
	val bottomMostPoint: Float = MelodyView.BOTTOM + (Orientation.normalizedDevicePitch() * orientationRange)
	val bottomMostNote: Int = Math.floor(bottomMostPoint.toDouble()).toInt()
	var noteIndex = 0
	var currentScreenNote = getOrCreateNote(
		tone = closestToneInChord(bottomMostNote),
		pressed = false,
		xMin = 0f,
		xMax = (bottomMostNote - bottomMostPoint) * width.toFloat() / halfStepsOnScreen,
		index = noteIndex++
	)
	(bottomMostNote..(bottomMostNote + halfStepsOnScreen + 1)).forEach {
		toneMaybeNotInChord ->
		val toneInChord = closestToneInChord(toneMaybeNotInChord)
		if(toneInChord != currentScreenNote.tone) {
			result.add(currentScreenNote)
			currentScreenNote = getOrCreateNote(
				tone = toneInChord,
				pressed = false,
				xMin = currentScreenNote.xMax,
				xMax = currentScreenNote.xMax,
				index = noteIndex++
			)
		}
		currentScreenNote.xMax += width.toFloat() / halfStepsOnScreen
	}
	result.add(currentScreenNote)
	return result
}