package com.jonlatane.beatpad.view.topology

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewPropertyAnimator
import java.util.concurrent.atomic.AtomicInteger

fun afterAll(animators: Collection<ViewPropertyAnimator>, action: () -> Unit) {
  val completed = AtomicInteger(0)
  for (animator in animators) {
    animator.withEndAction {
      if (completed.incrementAndGet() == animators.size) {
        action()
      }
    }.setDuration(ANIMATION_DURATION).start()
  }
}

fun ViewPropertyAnimator.scale(scale: Float) = scaleX(scale).scaleY(scale)!!
fun ViewPropertyAnimator.translationXY(translationX: Float, translationY: Float? = null)
  = translationX(translationX).translationY(translationY ?: translationX)!!

fun ViewPropertyAnimator.translationXYBy(translationX: Float, translationY: Float? = null)
  = translationXBy(translationX).translationYBy(translationY ?: translationX)!!

val View.density: Float get() = context.resources.displayMetrics.density
val View.scaledHeight get() = Math.round(height * scaleY)
val View.paddedHeight get() = Math.round((height + paddingTop + paddingBottom) * scaleY)
val View.scaledWidth get() = Math.round(width * scaleX)
val View.paddedWidth get() = Math.round((width + paddingLeft + paddingRight) * scaleX)

var View.scale: Float
  get() {
    if (scaleX == scaleY) return scaleX
    else throw UnsupportedOperationException()
  } set(value) {
  scaleX = value
  scaleY = value
}

var View.translationXY: Float
  get() {
    if (translationX == translationY) return translationX
    else throw UnsupportedOperationException()
  } set(value) {
  translationX = value
  translationY = value
}

var View.layoutWidth get() = this.layoutParams.width
  set(value) {
    val layoutParams = this.layoutParams
    layoutParams.width = value
    this.layoutParams = layoutParams
  }

var View.layoutHeight get() = this.layoutParams.height
  set(value) {
    val layoutParams = this.layoutParams
    layoutParams.height = value
    this.layoutParams = layoutParams
  }


fun View.animateWidth(width: Int) {
  val anim = ValueAnimator.ofInt(this.measuredWidth, width)
  anim.addUpdateListener { valueAnimator ->
    val value = valueAnimator.animatedValue as Int
    this.layoutWidth = value
  }
  anim.setDuration(ANIMATION_DURATION).start()
}

fun View.animateHeight(height: Int) {
  val anim = ValueAnimator.ofInt(this.measuredHeight, height)
  anim.addUpdateListener { valueAnimator ->
    val value = valueAnimator.animatedValue as Int
    this.layoutHeight = value
  }
  anim.setDuration(ANIMATION_DURATION).start()
}