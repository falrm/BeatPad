package com.jonlatane.beatpad.util

import android.animation.ValueAnimator
import android.text.TextUtils
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.LinearInterpolator
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.concurrent.atomic.AtomicInteger
import android.widget.TextView
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.singleLine
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.jonlatane.beatpad.view.orbifold.OrbifoldView


private val defaultDuration get() = 300L

interface HideableView {
	var initialHeight: Int?
	var initialWidth: Int?

  fun show(
    animated: Boolean = true,
    animation: HideAnimation = HideAnimation.VERTICAL
  ) {
    animation.apply {
      (this@HideableView as View).show(animated)
    }
  }

  fun hide(
    animated: Boolean = true,
    animation: HideAnimation = HideAnimation.VERTICAL
  ) {
    animation.apply {
      (this@HideableView as View).hide(animated)
    }
  }
}

fun TextView.toolbarTextStyle() {
	singleLine = true
	ellipsize = TextUtils.TruncateAt.MARQUEE
	marqueeRepeatLimit = -1
	isSelected = true
	allCaps = true
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
		layoutParams = layoutParams.apply { width = value }
	}

var View.layoutHeight get() = this.layoutParams.height
	set(value) {
		layoutParams = layoutParams.apply { height = value }
	}

fun View.animateWidth(width: Int, duration: Long = defaultDuration, endAction: (() -> Unit)? = null) {
	val anim = ValueAnimator.ofInt(measuredWidth, width)
	anim.addUpdateListener { valueAnimator ->
		layoutWidth = valueAnimator.animatedValue as Int
	}
	endAction?.let {
		anim.addListener(object : AnimatorListenerAdapter() {
			override fun onAnimationEnd(animation: Animator) = it()
		})
	}
	anim.setDuration(duration).start()
}

fun View.animateHeight(height: Int, duration: Long = defaultDuration) {
	val anim = ValueAnimator.ofInt(this.measuredHeight, height)
  anim.interpolator = LinearInterpolator()
	anim.addUpdateListener { valueAnimator ->
		this.layoutHeight = valueAnimator.animatedValue as Int
	}
	anim.setDuration(duration).start()
}

enum class HideAnimation: AnkoLogger {
  VERTICAL {
    override fun View.show(animated: Boolean) {
      setupHiding()
      if (animated) {
        animateHeight((this as HideableView).initialHeight!!)
      } else {
        layoutHeight = (this as HideableView).initialHeight!!
      }
    }

    override fun View.hide(animated: Boolean) {
      setupHiding()
      if (animated) {
        animateHeight(0)
      } else {
        layoutHeight = 0
      }
    }
  },
  HORIZONTAL {
    override fun View.show(animated: Boolean) {
      setupHiding()
      if (animated) {
        animateWidth((this as HideableView).initialWidth!!)
      } else {
        layoutWidth = (this as HideableView).initialWidth!!
      }
    }

    override fun View.hide(animated: Boolean) {
      setupHiding()
      if (animated) {
        animateWidth(0)
      } else {
        layoutWidth = 0
      }
    }
  };

  abstract fun View.show(animated: Boolean = true)

  abstract fun View.hide(animated: Boolean = true)

  fun View.setupHiding() {
    if ((this as HideableView).initialWidth == null || (this as HideableView).initialHeight == null) {
      measure(width, height)
      initialWidth = if (measuredWidth > 0) measuredWidth else layoutWidth
      initialHeight = if (measuredHeight > 0) measuredHeight else layoutHeight

      if(this is OrbifoldView) {
        info("HideAnimation Orbifold initialWidth=$initialWidth")
      }
    }
  }
}
val View.isHidden: Boolean get() = layoutHeight == 0 || layoutWidth == 0

fun View.color(resId: Int) = context.color(resId)

inline val RecyclerView.firstVisibleItemPosition
  get() = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
