package com.jonlatane.beatpad.view.harmony

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.util.color
import org.jetbrains.anko.*
import android.view.ViewGroup



class HarmonyElementView @JvmOverloads constructor(
  context: Context,
  var viewModel: HarmonyViewModel? = null
): _RelativeLayout(context) {
  val harmony: Harmony? get() = viewModel?.harmony
  var elementPosition = 0
  val element: Harmony.Element? get() = harmony?.elements?.get(elementPosition)
  val chord: Chord? get() = element?.chord
  inline val isDownbeat: Boolean get() = harmony?.run {
    elementPosition % subdivisionsPerBeat == 0
  } ?: false

  init {
    clipChildren = false
    clipToPadding = false
  }

  fun setAllParentsClip(enabled: Boolean) {
    var v: ViewGroup = this
    while (v.parent != null && v.parent is ViewGroup) {
      val viewGroup = v.parent as ViewGroup
      viewGroup.clipChildren = enabled
      viewGroup.clipToPadding = enabled
      v = viewGroup
    }
  }

  val chordText = textView {
    text = "hi"
    textSize = 20f
    maxLines = 1
    clipChildren = false
    clipToPadding = false
    //ellipsize  = TextUtils.TruncateAt.END
  }.lparams(wrapContent, wrapContent) {
    alignParentLeft()
    setMargins(dip(10), dip(5), dip(-100), dip(5))
  }


  private val paint = Paint()
  private var bounds = Rect()
  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.getClipBounds(bounds)
    canvas.drawRhythm()
  }

  override fun invalidate() {
    chordText.text = (element as? Harmony.Element.Change)?.chord?.name ?: ""
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

    var v: ViewGroup = this
    while (v.parent != null && v.parent is ViewGroup) {
      val viewGroup = v.parent as ViewGroup
      viewGroup.clipChildren = false
      viewGroup.clipToPadding = false
      v = viewGroup
    }
    super.invalidate()
  }

  private fun Canvas.drawRhythm() {
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