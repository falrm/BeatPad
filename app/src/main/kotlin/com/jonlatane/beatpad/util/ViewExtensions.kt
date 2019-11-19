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
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.SeekBar
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.view.orbifold.OrbifoldView


val defaultDuration get() = 300L

var SeekBar.thumbColorFilterColor: Int?
  get() = null
  set(value) {
    arrayOf(progressDrawable, thumb).forEach { drawable ->
      when(value) {
        null -> drawable.colorFilter = null
        else -> drawable.setColorFilter(value, PorterDuff.Mode.SRC_IN)
      }
    }
  }
interface HideableView {
	var initialHeight: Int?
	var initialWidth: Int?
  var initialTopMargin: Int?
  var initialBottomMargin: Int?
  var initialLeftMargin: Int?
  var initialRightMargin: Int?

  fun show(
    animated: Boolean = true,
    animation: HideAnimation = HideAnimation.VERTICAL,
    endAction: () -> Unit = {}
  ) {
    animation.apply {
      (this@HideableView as View).show(animated, endAction)
    }
  }

  fun hide(
    animated: Boolean = true,
    animation: HideAnimation = HideAnimation.VERTICAL,
    endAction: () -> Unit = {}
  ) {
    animation.apply {
      (this@HideableView as View).hide(animated, endAction)
    }
  }
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

var View.marginLayoutParams get() = this.layoutParams as ViewGroup.MarginLayoutParams
set(value) {
  this.layoutParams = value
}

var View.topMargin get() = this.marginLayoutParams.topMargin
  set(value) {
    layoutParams = marginLayoutParams.apply { topMargin = value }
  }
var View.bottomMargin get() = this.marginLayoutParams.bottomMargin
  set(value) {
    layoutParams = marginLayoutParams.apply { bottomMargin = value }
  }
var View.leftMargin get() = this.marginLayoutParams.leftMargin
  set(value) {
    layoutParams = marginLayoutParams.apply { leftMargin = value }
  }
var View.rightMargin get() = this.marginLayoutParams.rightMargin
  set(value) {
    layoutParams = marginLayoutParams.apply { rightMargin = value }
  }

fun View.animateWidth(width: Int, duration: Long = defaultDuration, endAction: (() -> Unit)? = null) {
	val anim = ValueAnimator.ofInt(this.width, width)
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

fun View.animateHeight(height: Int, duration: Long = defaultDuration, endAction: (() -> Unit)? = null) {
	val anim = ValueAnimator.ofInt(this.height, height)
  anim.interpolator = LinearInterpolator()
	anim.addUpdateListener { valueAnimator ->
		this.layoutHeight = valueAnimator.animatedValue as Int
	}
  endAction?.let {
    anim.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) = it()
    })
  }
	anim.setDuration(duration).start()
}

fun View.animateTopMargin(margin: Int, duration: Long = defaultDuration, endAction: (() -> Unit)? = null) {
  val anim = ValueAnimator.ofInt(this.topMargin, margin)
  anim.interpolator = LinearInterpolator()
  anim.addUpdateListener { valueAnimator ->
    this.topMargin = valueAnimator.animatedValue as Int
  }
  endAction?.let {
    anim.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) = it()
    })
  }
  anim.setDuration(duration).start()
}

fun View.animateBottomMargin(margin: Int, duration: Long = defaultDuration, endAction: (() -> Unit)? = null) {
  val anim = ValueAnimator.ofInt(this.bottomMargin, margin)
  anim.interpolator = LinearInterpolator()
  anim.addUpdateListener { valueAnimator ->
    this.bottomMargin = valueAnimator.animatedValue as Int
  }
  endAction?.let {
    anim.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) = it()
    })
  }
  anim.setDuration(duration).start()
}

fun View.animateLeftMargin(margin: Int, duration: Long = defaultDuration, endAction: (() -> Unit)? = null) {
  val anim = ValueAnimator.ofInt(this.leftMargin, margin)
  anim.interpolator = LinearInterpolator()
  anim.addUpdateListener { valueAnimator ->
    this.leftMargin = valueAnimator.animatedValue as Int
  }
  endAction?.let {
    anim.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) = it()
    })
  }
  anim.setDuration(duration).start()
}

fun View.animateRightMargin(margin: Int, duration: Long = defaultDuration, endAction: (() -> Unit)? = null) {
  val anim = ValueAnimator.ofInt(this.rightMargin, margin)
  anim.interpolator = LinearInterpolator()
  anim.addUpdateListener { valueAnimator ->
    this.rightMargin = valueAnimator.animatedValue as Int
  }
  endAction?.let {
    anim.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) = it()
    })
  }
  anim.setDuration(duration).start()
}

inline val Configuration.tablet: Boolean
  get() = smallestScreenWidthDp > 600

enum class HideAnimation: AnkoLogger {
  VERTICAL {
    override fun View.show(animated: Boolean, endAction: () -> Unit) {
      setupHiding()
      if (animated) {
        animateHeight((this as HideableView).initialHeight!!, endAction = endAction)
        animateTopMargin((this as HideableView).initialTopMargin!!, endAction = endAction)
        animateBottomMargin((this as HideableView).initialBottomMargin!!, endAction = endAction)
      } else {
        layoutHeight = (this as HideableView).initialHeight!!
        topMargin = (this as HideableView).initialTopMargin!!
        bottomMargin = (this as HideableView).initialBottomMargin!!
        endAction()
      }
    }

    override fun View.hide(animated: Boolean, endAction: () -> Unit) {
      setupHiding()
      if (animated) {
        animateHeight(0, endAction = endAction)
        animateTopMargin(0, endAction = endAction)
        animateBottomMargin(0, endAction = endAction)
      } else {
        layoutHeight = 0
        topMargin = 0
        bottomMargin = 0
        endAction()
      }
    }
  },
  HORIZONTAL {
    override fun View.show(animated: Boolean, endAction: () -> Unit) {
      setupHiding()
      if (animated) {
        animateWidth((this as HideableView).initialWidth!!, endAction = endAction)
      } else {
        layoutWidth = (this as HideableView).initialWidth!!
        endAction()
      }
    }

    override fun View.hide(animated: Boolean, endAction: () -> Unit) {
      setupHiding()
      if (animated) {
        animateWidth(0, endAction = endAction)
      } else {
        layoutWidth = 0
        endAction()
      }
    }
  },
  HORIZONTAL_ALPHA {
    override fun View.show(animated: Boolean, endAction: () -> Unit) {
      setupHiding()
      if (animated) {
        animateWidth((this as HideableView).initialWidth!!, duration = defaultDuration/2) {
          animate().alpha(1f).setDuration(defaultDuration/2).withEndAction {
            endAction()
          }.start()
        }
      } else {
        layoutWidth = (this as HideableView).initialWidth!!
        alpha = 1f
        endAction()
      }
    }

    override fun View.hide(animated: Boolean, endAction: () -> Unit) {
      setupHiding()
      if (animated) {
        animate().alpha(0f).setDuration(defaultDuration/2).withEndAction {
          animateWidth(0, duration = defaultDuration/2) {
            endAction()
          }
        }.start()
      } else {
        layoutWidth = 0
        alpha = 0f
        endAction()
      }
    }
  },
  VERTICAL_THEN_HORIZONTAL {
    override fun View.show(animated: Boolean, endAction: () -> Unit) {
      with(VERTICAL) {
        show(animated) {
          animate().alpha(1f)
          with(HORIZONTAL) {
            show(animated, endAction)
          }
        }
      }
    }

    override fun View.hide(animated: Boolean, endAction: () -> Unit) {
      animate().alpha(0f)
      with(VERTICAL) {
        hide(animated) {
          with(HORIZONTAL) {
            hide(animated, endAction)
          }
        }
      }
    }
  },
  HORIZONTAL_THEN_VERTICAL {
    override fun View.show(animated: Boolean, endAction: () -> Unit) {
      with(HORIZONTAL) {
        show(animated) {
          animate().alpha(1f)
          with(VERTICAL) {
            show(animated, endAction)
          }
        }
      }
    }

    override fun View.hide(animated: Boolean, endAction: () -> Unit) {
      animate().alpha(0f)
      with(HORIZONTAL) {
        hide(animated) {
          with(VERTICAL) {
            hide(animated, endAction)
          }
        }
      }
    }
  };

  abstract fun View.show(animated: Boolean = true, endAction: () -> Unit = {})
  abstract fun View.hide(animated: Boolean = true, endAction: () -> Unit = {})

  fun View.setupHiding() {
    if ((this as HideableView).initialWidth == null || (this as HideableView).initialHeight == null) {
      measure(width, height)
      initialWidth = when {
        layoutWidth >= 1  -> layoutWidth
        measuredWidth > 0 -> measuredWidth
        else              -> layoutWidth
      }
      initialHeight = when {
        layoutHeight >= 1  -> layoutHeight
        measuredHeight > 0 -> measuredHeight
        else               -> layoutHeight
      }
      initialTopMargin = topMargin
      initialBottomMargin = bottomMargin
      initialLeftMargin = leftMargin
      initialRightMargin = rightMargin

      if(this is OrbifoldView) {
        info("HideAnimation Orbifold initialWidth=$initialWidth")
      }
    }
  }
}
val View.isHidden: Boolean get() = layoutHeight == 0 || layoutWidth == 0

fun View.color(resId: Int) = context.color(resId)