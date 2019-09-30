package com.jonlatane.beatpad.view.melody.renderer

import android.graphics.Canvas
import android.graphics.Rect
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.util.size
import com.jonlatane.beatpad.view.colorboard.ColorGuide
import com.jonlatane.beatpad.view.melody.input.MelodyBeatEventHandlerBase
import com.jonlatane.beatpad.view.melody.MelodyBeatView
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.palette.BeatScratchToolbar
import org.jetbrains.anko.withAlpha

/**
 * This interface ain't threadsafe, yo. [chor]
 */
interface BaseMelodyBeatRenderer: ColorGuide, MelodyBeatEventHandlerBase {
  val viewModel: MelodyViewModel
  val overallBounds: Rect
  override val bounds: Rect
  val renderableToneBounds: Rect
  override var chord: Chord
  var isCurrentlyPlayingBeat: Boolean
  var isSelectedBeatInHarmony: Boolean
  val section: Section

  /**
   * Returns the range of indexes for the melody in this beat. i.e., for [beatPosition] = 3
   * of a melody in 4/2
   */
  fun elementRangeFor(melody: Melody<*>) =
    (beatPosition * melody.subdivisionsPerBeat) until
      (beatPosition + 1) * melody.subdivisionsPerBeat

  /**
   * The base method used to render a beat. Given the melody and our music-drawing view's
   * [beatPosition], subdivides the rendering area into slices based on [Melody.subdivisionsPerBeat]
   * and applies them to [bounds] (limiting memory swapping), and executes [stuff] providing the
   * element position. Meant for drawing, not threadsafe yo.
   *
   * Sets values for [chord], [isCurrentlyPlayingBeat], and [isSelectedBeatInHarmony].
   */
  fun Canvas.iterateSubdivisions(
    melody: Melody<*>,
    stuff: (Int) -> Unit // elementPosition
  ) {
    val elementRange: IntRange = elementRangeFor(melody)
    val elementCount = elementRange.size
    val overallWidth = overallBounds.right - overallBounds.left
    for((elementIndex, elementPosition) in elementRange.withIndex()) {
      bounds.apply {
        left = (overallWidth.toFloat() * elementIndex / elementCount).toInt()
        right = (overallWidth.toFloat() * (elementIndex + 1) / elementCount).toInt()
      }
      chord = chordAt(elementPosition, melody)
        ?: viewModel.paletteViewModel.orbifold.chord
          ?: MelodyBeatView.DEFAULT_CHORD
      isCurrentlyPlayingBeat =
        viewModel.paletteViewModel.playbackTick?.convertPatternIndex(
          fromSubdivisionsPerBeat = BeatClockPaletteConsumer.ticksPerBeat,
          toSubdivisionsPerBeat = melody.subdivisionsPerBeat
        ) == elementPosition
      isSelectedBeatInHarmony = viewModel.paletteViewModel.harmonyViewModel.selectedHarmonyElements
        ?.contains(elementPosition.convertPatternIndex(melody, harmony))
        ?: false
      stuff(elementPosition)
    }
  }

  fun Canvas.drawHorizontalLineInBounds(
    leftSide: Boolean = true,
    alphaSource: Float = 1f,
    strokeWidth: Float = 1f,
    startY: Float = bounds.top.toFloat(),
    stopY: Float = bounds.bottom.toFloat()
  ) {
    val oldStrokeWidth = paint.strokeWidth
    val oldColor = paint.color
    paint.color = 0xFF000000.toInt().withAlpha((alphaSource * 255).toInt())
    paint.strokeWidth = strokeWidth
    val x = if(leftSide) bounds.left.toFloat() else bounds.right.toFloat()
    drawLine(x, startY, x, stopY, paint)
    paint.strokeWidth = oldStrokeWidth
    paint.color = oldColor
  }

  fun invalidateDrawingLayer(): Unit
}