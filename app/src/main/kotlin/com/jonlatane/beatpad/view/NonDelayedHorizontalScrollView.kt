package com.jonlatane.beatpad.view

import android.content.Context
import android.os.Parcelable
import android.view.MotionEvent
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko._HorizontalScrollView
import org.jetbrains.anko.error

open class NonDelayedHorizontalScrollView(
	context: Context,
	var scrollingEnabled: Boolean = true
): _HorizontalScrollView(context), AnkoLogger {
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

	override fun onRestoreInstanceState(state: Parcelable?) {
		try {
			super.onRestoreInstanceState(state)
		} catch(t: Throwable) {
			error("Failed to restore instance state:", t)
		}
	}
}