package com.jonlatane.beatpad.view.colorboard

import android.graphics.Canvas
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.util.mod12
import com.jonlatane.beatpad.view.melody.MelodyBeatView
import org.jetbrains.anko.dip
import org.jetbrains.anko.withAlpha

interface ColorGuide : CanvasToneDrawer {
	var colorGuideAlpha: Int
	val drawPadding: Int
	val nonRootPadding: Int get() = MainApplication.instance.dip(13)
	val drawnColorGuideAlpha: Int // In case you want to scale by the View's alpha :)

  fun Canvas.drawColorGuide() {
    val halfStepPhysicalDistance = axisLength / halfStepsOnScreen
    for((tone, toneBounds) in visiblePitches) {
      val toneInChord = chord.closestTone(tone)
      paint.color = when((toneInChord - chord.root).mod12) {
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

      val extraPadding = if(toneInChord.mod12 == chord.root) 0 else nonRootPadding
      if(renderVertically) {
        drawRect(
          toneBounds.left + drawPadding + extraPadding,
          toneBounds.top,
          toneBounds.right - drawPadding - extraPadding,
          toneBounds.bottom,
          paint
        )
        if(tone == toneInChord) {
          paint.color = 0x11212121
          drawRect(
            toneBounds.left,
            toneBounds.top - .183f * halfStepPhysicalDistance,
            toneBounds.right,
            toneBounds.bottom + .183f * halfStepPhysicalDistance,
            paint
          )
        }
      } else {
        drawRect(
          toneBounds.left,
          toneBounds.top + drawPadding + extraPadding,
          toneBounds.right,
          toneBounds.bottom - drawPadding - extraPadding,
          paint
        )
				if(tone == toneInChord) {
					paint.color = 0x11212121
					drawRect(
						toneBounds.left + .183f * halfStepPhysicalDistance,
						toneBounds.top + drawPadding + extraPadding,
						toneBounds.right - .183f * halfStepPhysicalDistance,
						toneBounds.bottom - drawPadding - extraPadding,
						paint
					)
				}
      }
    }
  }

	fun Canvas.drawColorGuideOld() {
    val halfStepPhysicalDistance = axisLength / halfStepsOnScreen
		for((tone, _, rectBottom, rectTop, targetToneCenter) in onScreenNotes) {
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
        paint.color = 0x11212121.toInt()
        drawRect(
          bounds.left.toFloat() + drawPadding + extraPadding,
          bounds.height() - targetToneCenter + .183f * halfStepPhysicalDistance,
          bounds.right.toFloat() - drawPadding - extraPadding,
          bounds.height() - targetToneCenter - .183f * halfStepPhysicalDistance,
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
}