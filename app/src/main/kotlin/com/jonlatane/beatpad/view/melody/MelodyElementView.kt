package com.jonlatane.beatpad.view.melody

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.harmony.chord.Maj
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.service.let
import com.jonlatane.beatpad.output.service.convertPatternIndex
import com.jonlatane.beatpad.view.colorboard.AlphaDrawer
import com.jonlatane.beatpad.view.colorboard.BaseColorboardView
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip
import org.jetbrains.anko.warn


class MelodyElementView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : BaseColorboardView(context, attrs, defStyle), MelodyEventArticulationHandler, MelodyEventEditingHandler, AnkoLogger {

  init {
    showSteps = true
  }

  internal var viewModel: MelodyViewModel? = null

  override var elementPosition = 0
  override val melody: Melody<*>? get() = viewModel?.openedMelody
  inline val nextElement: Transposable<*>?
    get() = changes[elementPosition % (melody?.length ?: Int.MAX_VALUE)]
  inline val isDownbeat: Boolean
    get() = elementPosition % (melody?.subdivisionsPerBeat ?: Int.MAX_VALUE) == 0

  override val downPointers = SparseArray<PointF>()
  //override var initialHeight: Int? = null
  override val renderVertically = true
  override val halfStepsOnScreen = 88f
  override val drawPadding: Int
    get() = if (width > dip(37f)) dip(5)
    else Math.max(0, width - dip(32f))
  override val nonRootPadding get() = drawPadding
  private val harmony: Harmony? get() = (viewModel as? PaletteViewModel)?.harmonyViewModel?.harmony
  private val harmonyPosition: Int?
    get() = (melody to harmony).let { melody, harmony ->
      elementPosition.convertPatternIndex(melody, harmony)
    }
  private val harmonyChord: Chord?
    get() = (harmony to harmonyPosition).let { harmony, harmonyPosition ->
      harmony.changeBefore(harmonyPosition)
    }
  override var chord: Chord
    get() = harmonyChord ?: viewModel?.orbifold?.chord ?: Chord(0, Maj)
    set(_) = TODO("Why?")
  override val melodyOffset: Int
    get() = viewModel?.let { it.openedMelody?.offsetUnder(chord)  } ?: 0

  override fun onDraw(canvas: Canvas) {
    colorGuideAlpha = if (viewModel?.playbackPosition == elementPosition) 255 else 187
    super.onDraw(canvas)
    canvas.drawStepNotes()
    canvas.drawRhythm()
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    return when (viewModel?.melodyEditingModifiers?.modifier) {
      MelodyEditingModifiers.Modifier.None -> false
      MelodyEditingModifiers.Modifier.Editing -> onTouchEditEvent(event)
      MelodyEditingModifiers.Modifier.Articulating -> true//onTouchArticulateEvent(event)
      MelodyEditingModifiers.Modifier.Transposing -> true
      else -> false
    }
  }

  override fun getTone(y: Float): Int {
    return Math.round(lowestPitch + 88 * (height - y) / height)
  }

  private val p = Paint()
  private fun Canvas.drawStepNotes() {
    p.color = if (isChange) 0xAA212121.toInt() else 0xAA424242.toInt()

    try {
      val tones: Set<Int> = melody?.changeBefore(elementPosition).let {
        (it as? RationalMelody.Element)?.tones
      } ?: emptySet()

      if (tones.isNotEmpty()) {
        val leftMargin = if (isChange) drawPadding else 0
        val rightMargin = if (nextElement == null) drawPadding else 0
        tones.forEach { tone ->
          val realTone = tone + melodyOffset
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

  private fun Canvas.drawRhythm() {
    p.color = 0xAA212121.toInt()
    drawRect(
      bounds.left.toFloat(),
      bounds.top.toFloat(),
      bounds.left.toFloat() + if (isDownbeat) 10f else 5f,
      bounds.bottom.toFloat(),
      p
    )
  }
}
