package com.jonlatane.beatpad.view.melody.renderer

import android.graphics.Canvas
import android.graphics.Rect
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.melody.RationalMelody
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
  override val bounds: Rect
  val renderableToneBounds: Rect
  override var chord: Chord

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
   * and applies them to [bounds] (limiting memory swapping), and executes [stuff] with the arguments
   *
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
      chord = chordAt(elementPosition)
        ?: viewModel.paletteViewModel.orbifold.chord
          ?: MelodyBeatView.DEFAULT_CHORD
      stuff(elementPosition)
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

  fun Canvas.drawRhythm(
    melody: Melody<*>,
    elementPosition: Int,
    alphaSource: Float
  ) = drawHorizontalLineInBounds(
    strokeWidth = if (elementPosition % melody.subdivisionsPerBeat == 0) 5f else 1f
  )


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