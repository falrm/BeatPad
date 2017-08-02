package com.jonlatane.beatpad.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewManager
import android.widget.HorizontalScrollView
import org.jetbrains.anko._HorizontalScrollView
import org.jetbrains.anko.custom.ankoView

open class NonDelayedHorizontalScrollView(
	context: Context,
	var scrollingEnabled: Boolean = true
): _HorizontalScrollView(context) {
	override fun shouldDelayChildPressedState() = false
	override fun onTouchEvent(ev: MotionEvent): Boolean {
		when (ev.action) {
			MotionEvent.ACTION_DOWN -> {
				return scrollingEnabled && super.onTouchEvent(ev)
			}
			else -> return super.onTouchEvent(ev)
		}
	}
	override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
		return scrollingEnabled && super.onInterceptTouchEvent(ev)
	}
}