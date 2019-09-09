package com.jonlatane.beatpad.view.colorboard

import android.graphics.Canvas
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.util.mod12
import org.jetbrains.anko.withAlpha

interface Steps : AlphaDrawer {
	var colorGuideAlpha: Int
	var chord: Chord
	val drawPadding: Int
	val nonRootPadding: Int
	val drawnColorGuideAlpha: Int // In case you want to scale by the View's alpha :)

	fun Canvas.drawColorGuide() {
		for((tone, _, rectBottom, rectTop) in onScreenNotes) {
			paint.color = when((tone - chord.root).mod12) {
				0 -> color(R.color.tonic)
				1 -> color(R.color.flatTwo)
				2 -> color(R.color.two)
				3 -> color(R.color.flatThree)
				4 -> color(R.color.three)
				5 -> color(R.color.four)
				6 -> color(R.color.flatFive)
				7 -> color(R.color.five)
				8 -> color(R.color.sharpFive)
				9 -> color(R.color.six)
				10 -> color(R.color.flatSeven)
				11 -> color(R.color.seven)
				else -> throw IllegalStateException()
			}.withAlpha(drawnColorGuideAlpha)
			val extraPadding = if(tone.mod12 == chord.root) 0 else nonRootPadding
			if(renderVertically) {
				drawRect(
					bounds.left.toFloat() + drawPadding + extraPadding,
					bounds.height() - rectBottom,
					bounds.right.toFloat() - drawPadding - extraPadding,
					bounds.height() - rectTop, // backwards y-axis bullshittery
					paint
				)
			} else {
				drawRect(
					rectBottom,
					bounds.top.toFloat() + drawPadding + extraPadding,
					rectTop,
					bounds.bottom.toFloat() - drawPadding - extraPadding,
					paint
				)
			}
		}
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