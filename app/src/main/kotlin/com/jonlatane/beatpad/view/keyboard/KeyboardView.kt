package com.jonlatane.beatpad.view.keyboard

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.HorizontalScrollView

import com.jonlatane.beatpad.R

class KeyboardView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : HorizontalScrollView(context, attrs, defStyle) {
    val margin: Int = context.resources.displayMetrics.densityDpi/5

    init {
        LayoutInflater.from(context).inflate(R.layout.view_keyboard, this, true)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.getX() < margin || event.getX() > getWidth() - margin)
            enableScrolling()

        if (enableScrolling && event.getPointerCount() < 2)
            super.onTouchEvent(event)

        if (event.getActionMasked() == MotionEvent.ACTION_UP && event.getPointerCount() < 2)
            disableScrolling()
        return true
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && event.getX() < margin || event.getX() > getWidth() - margin)
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

    override protected fun onDraw(c: Canvas) {
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

    fun toggleVisibility() {
        if (isHidden) {
            show()
        } else {
            hide()
        }
    }

    fun show() {
        animate().translationY(0f).alpha(1f).start()
    }

    fun hide() {
        animate().translationY(getHeight().toFloat()).alpha(0f).start()
    }

    val isHidden: Boolean
        get() = getTranslationY() != 0f

    private var enableScrolling = false
    fun enableScrolling() {
        enableScrolling = true
    }

    fun disableScrolling() {
        enableScrolling = false
    }

    companion object {
        private val TAG = KeyboardView::class.simpleName
    }
}