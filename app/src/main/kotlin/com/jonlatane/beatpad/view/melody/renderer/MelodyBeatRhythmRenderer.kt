package com.jonlatane.beatpad.view.melody.renderer

import android.graphics.Canvas
import com.jonlatane.beatpad.model.Melody

interface MelodyBeatRhythmRenderer: BaseMelodyBeatRenderer {
  fun Canvas.drawRhythm(
    melody: Melody<*>,
    elementPosition: Int,
    alphaSource: Float
  ) = drawHorizontalLineInBounds(
    strokeWidth = if (elementPosition % melody.subdivisionsPerBeat == 0) 5f else 1f
  )
}