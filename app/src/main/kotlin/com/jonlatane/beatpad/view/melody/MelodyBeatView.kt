package com.jonlatane.beatpad.view.melody

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.service.convertPatternIndex
import com.jonlatane.beatpad.util.size
import com.jonlatane.beatpad.view.colorboard.AlphaDrawer
import com.jonlatane.beatpad.view.colorboard.BaseColorboardView
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*

/**
 * BeatViews
 */
class MelodyBeatView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0,
  override val viewModel: MelodyViewModel
) : BaseColorboardView(context, attrs, defStyle), MelodyBeatRenderer,
  MelodyBeatEventArticulationHandler, MelodyBeatEventEditingHandler, AnkoLogger {

  init {
    showSteps = true
  }

  override var beatPosition = 0
  override val melody: Melody<*>? get() = viewModel.openedMelody

  override val downPointers = SparseArray<PointF>()
  //override var initialHeight: Int? = null
  override val renderVertically = true
  override val halfStepsOnScreen = 88f
  inline val elementRange: IntRange? get() = melody?.let { elementRangeFor(it) }
  val drawWidth get() = elementRange?.let { (width.toFloat() / it.size).toInt() } ?: 0
  override val drawPadding: Int
    get() = if (drawWidth > dip(27f)) dip(5)
    else Math.max(0, drawWidth - dip(22f))
  override val nonRootPadding get() = drawPadding
  override val harmony: Harmony? get() = viewModel.paletteViewModel.harmonyViewModel.harmony

  override val overallBounds = Rect()
  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.getClipBounds(overallBounds)
//    val overallWidth = overallBounds.right - overallBounds.left
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
        drawColorGuide = melody.limitedToNotesInHarmony,
        drawRhythm = true
      )
    }

    BeatClockPaletteConsumer.section?.let { section ->
      section.melodies.filter { !it.isDisabled }.map { it.melody }.forEach { melody ->
        canvas.drawMelody(melody, drawAlpha = 66)
      }
    }
  }


  override fun melodyOffsetAt(elementPosition: Int): Int
    = (
      chordAt(elementPosition)
        ?: viewModel.paletteViewModel.orbifold.chord
        ?: DEFAULT_CHORD
    ).let { chord ->
    info("Computing edit under $chord")
      viewModel.let { it.openedMelody?.offsetUnder(chord) } ?: 0
    }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    return when (viewModel.melodyEditingModifiers.modifier) {
      MelodyEditingModifiers.Modifier.None -> false
      MelodyEditingModifiers.Modifier.Editing -> onTouchEditEvent(event)
      MelodyEditingModifiers.Modifier.Articulating -> true//onTouchArticulateEvent(event)
      MelodyEditingModifiers.Modifier.Transposing -> true
      else -> false
    }
  }

  override fun getPositionAndElement(x: Float): Pair<Int, Transposable<*>?>? {
    return melody?.let { melody ->
      val elementRange: IntRange = elementRange!!
      val elementIndex: Int = (elementRange.size * x / width).toInt()
      val elementPosition = Math.min(beatPosition * melody.subdivisionsPerBeat + elementIndex, melody.length - 1)
      return elementPosition to melody.changes[elementPosition]
    }
  }

  override fun getTone(y: Float): Int {
    return Math.round(lowestPitch + 88 * (height - y) / height)
  }

  override fun invalidateDrawingLayer() = invalidate()

  companion object {
    val DEFAULT_CHORD = Chord(0, intArrayOf(0,1,2,3,4,5,6,7,8,9,10,11))
  }
}
