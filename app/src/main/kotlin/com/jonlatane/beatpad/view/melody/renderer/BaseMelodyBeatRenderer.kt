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

/**
 * The primary thing of interest
 */
interface BaseMelodyBeatRenderer: ColorGuide, MelodyBeatEventHandlerBase {
  val viewModel: MelodyViewModel
  val overallBounds: Rect
  override var chord: Chord

  fun elementRangeFor(melody: Melody<*>) =
    (beatPosition * melody.subdivisionsPerBeat) until
      Math.min((beatPosition + 1) * melody.subdivisionsPerBeat, melody.length)


  fun Canvas.drawMelody(
    melody: Melody<*>,
    stepNoteAlpha: Int,
    drawRhythm: Boolean = false,
    drawColorGuide: Boolean = true,
    forceDrawColorGuideForCurrentBeat: Boolean = false,
    alphaSource: Float
  ) {
    iterateSubdivisions(melody) { elementIndex, elementPosition ->
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
      if(drawRhythm) drawRhythm(melody, elementIndex, alphaSource)
    }

    val overallWidth = overallBounds.right - overallBounds.left
    bounds.apply {
      left = overallWidth
      right = overallWidth
    }
    if(drawRhythm) drawRhythm(melody, melody.subdivisionsPerBeat, alphaSource)
  }


  fun Canvas.iterateSubdivisions(
    melody: Melody<*>,
    stuff: (Int, Int) -> Unit //elementIndex, elementPosition
  ) {
    val elementRange: IntRange = elementRangeFor(melody) /*(beatPosition * melody.subdivisionsPerBeat) until
          Math.min((beatPosition + 1) * melody.subdivisionsPerBeat, melody.length - 1)*/
    val elementCount = elementRange.size
    val overallWidth = overallBounds.right - overallBounds.left
    for((elementIndex, elementPosition) in elementRange.withIndex()) {
      bounds.apply {
        left = (overallWidth.toFloat() * elementIndex / elementCount).toInt()
        right = (overallWidth.toFloat() * (elementIndex + 1) / elementCount).toInt()
      }
      chord = chordAt(elementPosition)
        ?: viewModel.paletteViewModel.orbifold.chord
          ?: MelodyBeatView.DEFAULT_CHORD
      stuff(elementIndex, elementPosition)
    }
  }

  fun Canvas.drawStepNotes(
    melody: Melody<*>,
    elementPosition: Int,
    drawAlpha: Int = 0xAA,
    alphaSource: Float
  ) {
    val element: Transposable<*>? = melody.changes[elementPosition]
    val nextElement: Transposable<*>? = melody.changes[elementPosition]
    val isChange = element != null
    paint.color = (if (isChange) 0xAA212121.toInt() else 0xAA424242.toInt()).withAlpha((alphaSource * drawAlpha).toInt())

    try {
      val tones: Set<Int> = melody.changeBefore(elementPosition).let {
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

  fun Canvas.drawRhythm(melody: Melody<*>, elementIndex: Int, alphaSource: Float) {
    paint.color = 0xAA212121.toInt().withAlpha((alphaSource * 255).toInt())
    val halfWidth = if (elementIndex % melody.subdivisionsPerBeat == 0) 5f else 1f
    drawRect(
      bounds.left.toFloat() - halfWidth,
      bounds.top.toFloat(),
      bounds.left.toFloat() + halfWidth,
      bounds.bottom.toFloat(),
      paint
    )
  }

  fun invalidateDrawingLayer(): Unit
}