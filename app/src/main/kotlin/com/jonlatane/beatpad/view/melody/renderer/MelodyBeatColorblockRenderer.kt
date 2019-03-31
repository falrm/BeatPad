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

interface MelodyBeatColorblockRenderer: ColorGuide, MelodyBeatEventHandlerBase {
  val colorblockAlpha: Float
  val viewModel: MelodyViewModel
  val overallBounds: Rect
  override var chord: Chord

  fun MelodyBeatView.renderColorblockMelodyBeat(canvas: Canvas) {
    canvas.getClipBounds(overallBounds)
    bounds.apply {
      top = overallBounds.top
      bottom = overallBounds.bottom
      left = overallBounds.left
      right = overallBounds.right
    }
    canvas.renderSteps()
    melody?.let { melody ->

      canvas.drawMelody(
        melody,
        drawAlpha = 0xAA,
        drawRhythm = true
      )
    }

    BeatClockPaletteConsumer.section?.let { section ->
      section.melodies.filter { !it.isDisabled }.map { it.melody }.forEach { melody ->
        canvas.drawMelody(melody, drawAlpha = 66, isBackgroundMelody = true)
      }
    }
  }

  fun Canvas.drawMelody(
    melody: Melody<*>,
    drawAlpha: Int,
    drawRhythm: Boolean = false,
    isBackgroundMelody: Boolean = false
  ) {
    val elementRange: IntRange = elementRangeFor(melody) /*(beatPosition * melody.subdivisionsPerBeat) until
          Math.min((beatPosition + 1) * melody.subdivisionsPerBeat, melody.length - 1)*/
    val elementCount = elementRange.size
    val overallWidth = overallBounds.right - overallBounds.left
    for((elementIndex, elementPosition) in elementRange.withIndex()) {
      bounds.apply {
        left = (overallWidth.toFloat() * elementIndex / elementCount).toInt()
        right = (overallWidth.toFloat() * (elementIndex+1) / elementCount).toInt()
      }
      chord = chordAt(elementPosition)
        ?: viewModel.paletteViewModel.orbifold.chord
          ?: MelodyBeatView.DEFAULT_CHORD
      val isCurrentlyPlayingBeat: Boolean =
        viewModel.paletteViewModel.playbackTick?.convertPatternIndex(
          from = BeatClockPaletteConsumer.ticksPerBeat,
          to = melody
        ) == elementPosition
      colorGuideAlpha = (when {
        isBackgroundMelody -> 0
        melody.limitedToNotesInHarmony -> when {
          isCurrentlyPlayingBeat -> 255
          else -> 187
        }
        isCurrentlyPlayingBeat -> 119
        else -> 0
      } * colorblockAlpha).toInt()
      if(colorGuideAlpha > 0) drawColorGuide()
      drawStepNotes(melody, elementPosition, drawAlpha)
      if(drawRhythm) drawRhythm(melody, elementIndex)
    }

    bounds.apply {
      left = overallWidth
      right = overallWidth
    }
    if(drawRhythm) drawRhythm(melody, melody.subdivisionsPerBeat)
  }

  fun Canvas.drawStepNotes(melody: Melody<*>, elementPosition: Int, drawAlpha: Int = 0xAA) {
    val element: Transposable<*>? = melody.changes[elementPosition]
    val nextElement: Transposable<*>? = melody.changes[elementPosition]
    val isChange = element != null
    paint.color = (if (isChange) 0xAA212121.toInt() else 0xAA424242.toInt()).withAlpha((colorblockAlpha * drawAlpha).toInt())

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

  fun Canvas.drawRhythm(melody: Melody<*>, elementIndex: Int) {
    paint.color = 0xAA212121.toInt()
    val halfWidth = if (elementIndex % melody.subdivisionsPerBeat == 0) 5f else 1f
    drawRect(
      bounds.left.toFloat() - halfWidth,
      bounds.top.toFloat(),
      bounds.left.toFloat() + halfWidth,
      bounds.bottom.toFloat(),
      paint
    )
  }


  fun elementRangeFor(melody: Melody<*>) =
    (beatPosition * melody.subdivisionsPerBeat) until
      Math.min((beatPosition + 1) * melody.subdivisionsPerBeat, melody.length)

  fun invalidateDrawingLayer()
}