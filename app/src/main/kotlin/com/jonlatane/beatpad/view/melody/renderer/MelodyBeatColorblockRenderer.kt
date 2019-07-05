package com.jonlatane.beatpad.view.melody.renderer

import BeatClockPaletteConsumer
import android.graphics.Canvas
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.service.convertPatternIndex
import com.jonlatane.beatpad.view.colorboard.AlphaDrawer
import com.jonlatane.beatpad.view.melody.MelodyBeatView
import org.jetbrains.anko.warn
import org.jetbrains.anko.withAlpha

interface MelodyBeatColorblockRenderer: BaseMelodyBeatRenderer, MelodyBeatRhythmRenderer {
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
          fromSubdivisionsPerBeat = BeatClockPaletteConsumer.ticksPerBeat,
          toSubdivisionsPerBeat = melody.subdivisionsPerBeat
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
      drawColorblockNotes(melody, elementPosition, stepNoteAlpha, alphaSource)
      if(drawRhythm) drawRhythm(melody, elementPosition, alphaSource)
    }

    val overallWidth = overallBounds.right - overallBounds.left
    bounds.apply {
      left = overallWidth
      right = overallWidth
    }
    if(drawRhythm) drawRhythm(melody, melody.subdivisionsPerBeat, alphaSource)
  }



  fun Canvas.drawColorblockNotes(
    melody: Melody<*>,
    elementPosition: Int,
    drawAlpha: Int = 0xAA,
    alphaSource: Float
  ) {
    val element: Transposable<*>? = melody.changes[elementPosition % melody.length]
    val nextElement: Transposable<*>? = melody.changes[elementPosition % melody.length]
    val isChange = element != null
    paint.color = (if (isChange) 0xAA212121.toInt() else 0xAA424242.toInt()).withAlpha((alphaSource * drawAlpha).toInt())

    try {
      val tones: Set<Int> = element?.let {
        (it as? RationalMelody.Element)?.tones
      } ?: emptySet()

      if (tones.isNotEmpty()) {
        val leftMargin = if (isChange) drawPadding else 0
        val rightMargin = if (nextElement != null) drawPadding else 0
        tones.forEach { tone ->
          val realTone = tone + melody.offsetUnder(chord)
          val top = height - height * (realTone - AlphaDrawer.BOTTOM) / 88f
          val bottom = height - height * (realTone - AlphaDrawer.BOTTOM + 1) / 88f
          drawRect(
            bounds.left.toFloat() + leftMargin,
            top,
            bounds.right.toFloat() - rightMargin,
            bottom,
            paint
          )
        }
      }
    } catch (t: Throwable) {
      warn("Error drawing pressed notes in sequence", t)
      invalidateDrawingLayer()
    }
  }
}