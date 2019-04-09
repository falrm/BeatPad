package com.jonlatane.beatpad.view.melody.renderer

import android.graphics.Canvas
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.view.colorboard.CanvasToneDrawer
import org.jetbrains.anko.withAlpha

interface MelodyBeatNotationRenderer: BaseMelodyBeatRenderer, CanvasToneDrawer {
  val notationAlpha: Float

  fun renderNotationMelodyBeat(canvas: Canvas) {
    canvas.renderStaffLines()
    melody?.let { melody ->
      canvas.drawMelody(
        melody,
        stepNoteAlpha = 255,
        drawRhythm = false,
        drawColorGuide = false,
        forceDrawColorGuideForCurrentBeat = true,
        alphaSource = notationAlpha
      )
    }
  }

  fun Canvas.renderStaffLines() {
    paint.color = color(R.color.colorPrimaryDark).withAlpha((255 * notationAlpha).toInt())
    val halfStepWidth: Float = axisLength / halfStepsOnScreen
    listOf(Clef.TREBLE, Clef.BASS).flatMap { it.tones.toList() }.forEach{ clefTone ->
      val linePosition = startPoint - (bottomMostNote + clefTone + 2.5f) * halfStepWidth
      if (renderVertically) {
        drawLine(
          bounds.left.toFloat(),
          linePosition,
          bounds.right.toFloat(),
          linePosition,
          paint
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
    }
    /*
    var linePosition = startPoint - 12 * halfStepWidth
    while (linePosition < axisLength) {
      if (renderVertically) {
        drawLine(
          bounds.left.toFloat(),
          linePosition,
          bounds.right.toFloat(),
          linePosition,
          paint
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
    }*/
  }


}