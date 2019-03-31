package com.jonlatane.beatpad.view.melody.renderer

import android.graphics.Canvas
import android.graphics.Paint
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.view.colorboard.CanvasToneDrawer
import org.jetbrains.anko.withAlpha

interface MelodyBeatNotationRenderer: CanvasToneDrawer {
  val notationAlpha: Float

  fun renderNotationMelodyBeat(canvas: Canvas) {

  }

  fun Canvas.renderStaffLines() {
    paint.color = color(R.color.colorPrimaryDark).withAlpha((255 * notationAlpha).toInt())
    val halfStepWidth: Float = axisLength / halfStepsOnScreen
    var linePosition = startPoint - 12 * halfStepWidth
    while (linePosition < axisLength) {
      if (renderVertically) {
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