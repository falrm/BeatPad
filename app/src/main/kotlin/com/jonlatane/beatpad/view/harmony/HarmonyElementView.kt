package com.jonlatane.beatpad.view.harmony

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.util.color
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp
import org.jetbrains.anko.textColor

class HarmonyElementView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0,
  var viewModel: HarmonyViewModel? = null
): TextView(context, attrs, defStyle) {
  val harmony: Harmony? get() = viewModel?.harmony
  var elementPosition = 0
  val element: Harmony.Element? get() = harmony?.elements?.get(elementPosition)
  val chord: Chord? get() = element?.chord
  inline val isDownbeat: Boolean get() = harmony?.run {
    elementPosition % subdivisionsPerBeat == 0
  } ?: false


  init {
    textSize = 20f
    maxLines = 1
    ellipsize
  }
  private val paint = Paint()
  private var bounds = Rect()
  override fun onDraw(canvas: Canvas) {
    text = "  ${(element as? Harmony.Element.Change)?.chord?.name ?: ""}"
    backgroundColor = chord?.run {
      when {
        isDominant -> color(R.color.dominant)
        isDiminished -> color(R.color.diminished)
        isMinor -> color(R.color.minor)
        isAugmented -> color(R.color.augmented)
        isMajor -> color(R.color.major)
        else -> null
      }
    } ?: color(android.R.color.white)
    super.onDraw(canvas)
    canvas.getClipBounds(bounds)
    canvas.drawRhythm()
  }

  private fun Canvas.drawRhythm() {
    val x = bounds.left.toFloat() + if(isDownbeat) 10f else 1f
    paint.color = 0xAA212121.toInt()
    drawRect(
      bounds.left.toFloat(),
      bounds.top.toFloat(),
      bounds.left.toFloat() + if(isDownbeat) 10f else 1f,
      bounds.bottom.toFloat(),
      paint
    )
  }
}