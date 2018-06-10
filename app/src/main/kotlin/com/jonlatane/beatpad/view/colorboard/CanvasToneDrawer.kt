package com.jonlatane.beatpad.view.colorboard

import android.graphics.Canvas
import android.graphics.Paint
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.util.color

interface CanvasToneDrawer: AlphaDrawer {
	val showSteps: Boolean
	val chord: Chord

	// Renders the dividers that separate A, A#, B, C, etc. visually to the user
	fun Canvas.renderSteps() {
		paint.color = color(R.color.colorPrimaryDark)
		if(showSteps) {
			val halfStepWidth: Float = axisLength / halfStepsOnScreen
			var linePosition = onScreenNotes.first().top - 12 * halfStepWidth //TODO gross hack
			while(linePosition < axisLength) {
				if(renderVertically) {
					drawLine(
						bounds.left.toFloat(),
						linePosition,
						bounds.right.toFloat(),
						linePosition,
						Paint()
					)
				} else {
					drawLine(
						linePosition,
						bounds.top.toFloat(),
						linePosition,
						bounds.bottom.toFloat(),
						paint
					)
				}
				linePosition += halfStepWidth
			}
		}
	}

	/** Renders the lines for G2, B2, D3, F3, A3 (bass clef) and E4, G4, B4, D5, F5 (treble clef) */
	fun Canvas.renderGrandStaffLines() {
		paint.color = color(R.color.colorPrimaryDark)
		//TODO implement this.
	}


	data class OnScreenNote(
		var tone: Int = 0,
		var pressed: Boolean = false,
		var bottom: Float = 0f,
		var top: Float = 0f
	)

	val onScreenNotes: List<OnScreenNote> get() {
		val result = kotlin.collections.mutableListOf<OnScreenNote>()
		val orientationRange = highestPitch - lowestPitch + 1 - halfStepsOnScreen
		val bottomMostPoint: Float = lowestPitch + (Orientation.normalizedDevicePitch() * orientationRange)
		val bottomMostNote: Int = java.lang.Math.floor(bottomMostPoint.toDouble()).toInt()
		var currentScreenNote = OnScreenNote(
			tone = chord.closestTone(bottomMostNote),
			pressed = false,
			bottom = 0f,
			top = (bottomMostNote - bottomMostPoint) * axisLength / halfStepsOnScreen
		)
		for (toneMaybeNotInChord in (bottomMostNote..(bottomMostNote + halfStepsOnScreen.toInt() + 2))) {
			val toneInChord = chord.closestTone(toneMaybeNotInChord)
			if (toneInChord != currentScreenNote.tone) {
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
}