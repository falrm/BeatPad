package com.jonlatane.beatpad.view.melody.renderer

import BeatClockPaletteConsumer
import android.graphics.Canvas
import android.graphics.Rect
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.service.convertPatternIndex
import com.jonlatane.beatpad.util.size
import com.jonlatane.beatpad.view.colorboard.AlphaDrawer
import com.jonlatane.beatpad.view.colorboard.ColorGuide
import com.jonlatane.beatpad.view.melody.MelodyBeatEventHandlerBase
import com.jonlatane.beatpad.view.melody.MelodyBeatView
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import org.jetbrains.anko.warn
import org.jetbrains.anko.withAlpha

interface MelodyBeatColorblockRenderer: BaseMelodyBeatRenderer, MelodyBeatEventHandlerBase {
  val colorblockAlpha: Float
  override var chord: Chord

  fun MelodyBeatView.renderColorblockMelodyBeat(canvas: Canvas) {
    paint.strokeWidth = 0f
    canvas.renderSteps()
    melody?.let { melody ->
      val alphaMultiplier = if(viewModel.isMelodyReferenceEnabled) 1f else 2f/3
      canvas.drawMelody(
        melody,
        stepNoteAlpha = (0xAA * colorblockAlpha * alphaMultiplier).toInt(),
        drawRhythm = true,
        alphaSource = colorblockAlpha * alphaMultiplier
      )
    }

    BeatClockPaletteConsumer.section?.let { section ->
      section.melodies.filter { !it.isDisabled }.filter {
        when(melody?.limitedToNotesInHarmony) {
          null -> false
          true -> it.melody.limitedToNotesInHarmony
          false -> !it.melody.limitedToNotesInHarmony
        }
      }.map { it.melody }.forEach { melody ->
        canvas.drawMelody(
          melody,
          stepNoteAlpha = 66,
          drawColorGuide = false,
          alphaSource = colorblockAlpha
        )
      }
    }
  }

  fun Canvas.drawMelody(
    melody: Melody<*>,
    stepNoteAlpha: Int,
    drawRhythm: Boolean = false,
    drawColorGuide: Boolean = true,
    forceDrawColorGuideForCurrentBeat: Boolean = false,
    alphaSource: Float
  ) {
    iterateSubdivisions(melody) { elementPosition ->
      val isCurrentlyPlayingBeat: Boolean =
        viewModel.paletteViewModel.playbackTick?.convertPatternIndex(
          from = BeatClockPaletteConsumer.ticksPerBeat,
          to = melody
        ) == elementPosition
      colorGuideAlpha = (when {
        !drawColorGuide -> when {
          forceDrawColorGuideForCurrentBeat && isCurrentlyPlayingBeat -> 119
          else -> 0
        }
        melody.limitedToNotesInHarmony -> when {
          isCurrentlyPlayingBeat -> 255
          else -> 187
        }
        isCurrentlyPlayingBeat         -> 119
        else                           -> 0
      } * alphaSource).toInt()
      if(colorGuideAlpha > 0) {
        drawColorGuide()
      }
      drawStepNotes(melody, elementPosition, stepNoteAlpha, alphaSource)
      if(drawRhythm) drawRhythm(melody, elementPosition, alphaSource)
    }

    val overallWidth = overallBounds.right - overallBounds.left
    bounds.apply {
      left = overallWidth
      right = overallWidth
    }
    if(drawRhythm) drawRhythm(melody, melody.subdivisionsPerBeat, alphaSource)
  }
}