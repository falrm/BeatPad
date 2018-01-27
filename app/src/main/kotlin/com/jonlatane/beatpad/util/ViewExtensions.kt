package com.jonlatane.beatpad.util

import android.animation.ValueAnimator
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.*
import android.view.ViewPropertyAnimator
import android.view.animation.LinearInterpolator
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.concurrent.atomic.AtomicInteger
import android.view.animation.Transformation
import android.view.animation.Animation



private val defaultDuration get() = 300L

interface HideableView {
	var initialHeight: Int?
}

var nextViewId: Int = 10001
	get() = if(field == Int.MAX_VALUE) 101 else field++
  private set

fun afterAll(
	animators: Collection<ViewPropertyAnimator>,
	duration: Long = defaultDuration,
	action: () -> Unit
) {
	val completed = AtomicInteger(0)
	for (animator in animators) {
		animator.withEndAction {
			if (completed.incrementAndGet() == animators.size) {
				action()
			}
		}.setDuration(duration).start()
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


fun View.animateWidth(width: Int, duration: Long = defaultDuration) {
	val anim = ValueAnimator.ofInt(this.measuredWidth, width)
	anim.addUpdateListener { valueAnimator ->
		val value = valueAnimator.animatedValue as Int
		this.layoutWidth = value
	}
	anim.setDuration(duration).start()
}

fun View.animateHeight(height: Int, duration: Long = defaultDuration) {
	val anim = ValueAnimator.ofInt(this.measuredHeight, height)
  anim.interpolator = LinearInterpolator()
	anim.addUpdateListener { valueAnimator ->
		val value = valueAnimator.animatedValue as Int
		this.layoutHeight = value
	}
	anim.setDuration(duration).start()
}

val View.isHidden: Boolean get() = layoutHeight == 0
fun View.show(animated: Boolean = true) {
	if (animated) {
		animateHeight((this as HideableView).initialHeight!!)
	} else {
		layoutHeight = (this as HideableView).initialHeight!!
	}
}

fun View.hide(animated: Boolean = true) {
	if ((this as HideableView).initialHeight == null) {
		measure(width, height)
		initialHeight = if (measuredHeight > 0) measuredHeight else layoutHeight
	}
	if (animated) {
		animateHeight(0)
	} else {
		layoutHeight = 0
	}
}

fun View.color(resId: Int) = context.color(resId)
