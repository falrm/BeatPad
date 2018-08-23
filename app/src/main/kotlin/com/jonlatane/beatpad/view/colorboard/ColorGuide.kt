package com.jonlatane.beatpad.view.colorboard

import android.graphics.Canvas
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.util.mod12
import org.jetbrains.anko.dip
import org.jetbrains.anko.withAlpha

interface ColorGuide : CanvasToneDrawer {
	var colorGuideAlpha: Int
	val drawPadding: Int
	val nonRootPadding: Int get() = MainApplication.instance.dip(13)
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
}