package com.jonlatane.beatpad.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewManager
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import org.jetbrains.anko._ScrollView
import org.jetbrains.anko.custom.ankoView

class NonDelayedScrollView @JvmOverloads constructor(
	context: Context,
	var scrollingEnabled: Boolean = true
) : _ScrollView(context) {
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
