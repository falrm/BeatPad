package com.jonlatane.beatpad.view.keyboard

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewManager
import android.widget.HorizontalScrollView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.HideableView
import org.jetbrains.anko.custom.ankoView

class KeyboardView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : HorizontalScrollView(context, attrs, defStyle), HideableView {
  override var initialHeight: Int? = null
  val margin: Int = context.resources.displayMetrics.densityDpi / 5

  init {
    LayoutInflater.from(context).inflate(R.layout.view_keyboard, this, true)
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (event.x < margin || event.x > width - margin)
      enableScrolling()

    if (enableScrolling && event.pointerCount < 2)
      super.onTouchEvent(event)

    if (event.actionMasked == MotionEvent.ACTION_UP && event.pointerCount < 2)
      disableScrolling()
    return true
  }

  override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
    if (event.actionMasked == MotionEvent.ACTION_DOWN && event.x < margin || event.x > width - margin)
      return true
    return false
  }

  private var leftShadowAlpha = 255
  private var rightShadowAlpha = 255
  private val leftShadowEvaluator = object : IntEvaluator() {
    override fun evaluate(fraction: Float, startValue: Int, endValue: Int): Int {
      val num = super.evaluate(fraction, startValue, endValue)
      leftShadowAlpha = num
      return num
    }
  }
  private val rightShadowEvaluator = object : IntEvaluator() {
    override fun evaluate(fraction: Float, startValue: Int, endValue: Int): Int {
      val num = super.evaluate(fraction, startValue, endValue)
      rightShadowAlpha = num
      return num
    }
  }

  override fun onDraw(c: Canvas) {
    super.onDraw(c)

    if (canScrollHorizontally(-1))
      ValueAnimator.ofObject(leftShadowEvaluator, leftShadowAlpha, 255).start()
    else
      ValueAnimator.ofObject(leftShadowEvaluator, leftShadowAlpha, 255).start()
    if (canScrollHorizontally(1))
      ValueAnimator.ofObject(rightShadowEvaluator, rightShadowAlpha, 255).start()
    else
      ValueAnimator.ofObject(rightShadowEvaluator, rightShadowAlpha, 255).start()
  }

  override fun shouldDelayChildPressedState(): Boolean {
    return false
  }

  private var enableScrolling = false
  fun enableScrolling() {
    enableScrolling = true
  }

  fun disableScrolling() {
    enableScrolling = false
  }
}