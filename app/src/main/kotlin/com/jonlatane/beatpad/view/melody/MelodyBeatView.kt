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
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip
import org.jetbrains.anko.warn

/**
 * BeatViews
 */
class MelodyBeatView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0,
  val viewModel: MelodyViewModel
) : BaseColorboardView(context, attrs, defStyle), MelodyBeatEventArticulationHandler, MelodyBeatEventEditingHandler, AnkoLogger {

  init {
    showSteps = true
  }

  override var beatPosition = 0
  override val melody: Melody<*>? get() = viewModel.openedMelody

  override val downPointers = SparseArray<PointF>()
  //override var initialHeight: Int? = null
  override val renderVertically = true
  override val halfStepsOnScreen = 88f
  inline val elementRange: IntRange? get() = melody?.let { melody ->
    (beatPosition * melody.subdivisionsPerBeat) until
      Math.min((beatPosition + 1) * melody.subdivisionsPerBeat, melody.length)
  }
  val drawWidth get() = elementRange?.let { (width.toFloat() / it.size).toInt() } ?: 0
  override val drawPadding: Int
    get() = if (drawWidth > dip(37f)) dip(5)
    else Math.max(0, drawWidth - dip(32f))
  override val nonRootPadding get() = drawPadding
  private val harmony: Harmony? get() = (viewModel as? PaletteViewModel)?.harmonyViewModel?.harmony

  private val overallBounds = Rect()
  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.getClipBounds(overallBounds)
    val overallWidth = overallBounds.right - overallBounds.left
    bounds.apply {
      top = overallBounds.top
      bottom = overallBounds.bottom
      left = overallBounds.left
      right = overallBounds.right
    }
    canvas.renderSteps()
    melody?.let { melody ->
      val elementRange: IntRange = elementRange!! /*(beatPosition * melody.subdivisionsPerBeat) until
          Math.min((beatPosition + 1) * melody.subdivisionsPerBeat, melody.length - 1)*/
      val elementCount = elementRange.size
      for((elementIndex, elementPosition) in elementRange.withIndex()) {
        bounds.apply {
          left = (overallWidth.toFloat() * elementIndex / elementCount).toInt()
          right = (overallWidth.toFloat() * (elementIndex+1) / elementCount).toInt()
        }
        chord = harmony?.let { harmony ->
          val harmonyPosition = elementPosition.convertPatternIndex(melody, harmony)
          harmony.changeBefore(harmonyPosition)
        } ?: viewModel.paletteViewModel.orbifold?.chord ?: DEFAULT_CHORD
        colorGuideAlpha = if (
          viewModel.paletteViewModel.playbackTick?.convertPatternIndex(
            from = BeatClockPaletteConsumer.ticksPerBeat,
            to = melody
          ) == elementPosition
        ) 255 else 187
        canvas.drawColorGuide()
        canvas.drawStepNotes(melody, elementPosition)
        canvas.drawRhythm(elementIndex)
      }
    }
  }

  //override val chord: Chord get() = super.chord
  override fun melodyOffsetAt(elementPosition: Int) = viewModel?.let { it.openedMelody?.offsetUnder(chord)  } ?: 0

  override fun onTouchEvent(event: MotionEvent): Boolean {
    return when (viewModel?.melodyEditingModifiers?.modifier) {
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
      return elementIndex to melody.changes[elementPosition]
    }
  }

  override fun getTone(y: Float): Int {
    return Math.round(lowestPitch + 88 * (height - y) / height)
  }

  private val p = Paint()
  private fun Canvas.drawStepNotes(melody: Melody<*>, elementPosition: Int) {
    val element: Transposable<*>? = melody.changes[elementPosition]
    val nextElement: Transposable<*>? = melody.changes[elementPosition]
    val isChange = element != null
    p.color = if (isChange) 0xAA212121.toInt() else 0xAA424242.toInt()

    try {
      val tones: Set<Int> = melody.changeBefore(elementPosition).let {
        (it as? RationalMelody.Element)?.tones
      } ?: emptySet()

      if (tones.isNotEmpty()) {
        val leftMargin = if (isChange) drawPadding else 0
        val rightMargin = if (nextElement == null) drawPadding else 0
        tones.forEach { tone ->
          val realTone = tone + melodyOffsetAt(elementPosition)
          val top = height - height * (realTone - AlphaDrawer.BOTTOM) / 88f
          val bottom = height - height * (realTone - AlphaDrawer.BOTTOM + 1) / 88f
          drawRect(
            bounds.left.toFloat() + leftMargin,
            top,
            bounds.right.toFloat() - rightMargin,
            bottom,
            p
          )
        }
      }
    } catch (t: Throwable) {
      warn("Error drawing pressed notes in sequence", t)
      invalidate()
    }
  }

  private fun Canvas.drawRhythm(elementIndex: Int) {
    p.color = 0xAA212121.toInt()
    drawRect(
      bounds.left.toFloat(),
      bounds.top.toFloat(),
      bounds.left.toFloat() + if (elementIndex == 0) 10f else 5f,
      bounds.bottom.toFloat(),
      p
    )
  }

  companion object {
    private val DEFAULT_CHORD = Chord(0, intArrayOf(0,1,2,3,4,5,6,7,8,9,10,11))
  }
}
