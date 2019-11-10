package com.jonlatane.beatpad.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewManager
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import com.jonlatane.beatpad.util.HideableView
import org.jetbrains.anko._ScrollView
import org.jetbrains.anko.custom.ankoView

open class NonDelayedScrollView @JvmOverloads constructor(
	context: Context,
	var scrollingEnabled: Boolean = true
) : _ScrollView(context), HideableView {
	override var initialHeight: Int? = null
	override var initialWidth: Int? = null
	override var initialTopMargin: Int? = null
	override var initialBottomMargin: Int? = null
	override var initialLeftMargin: Int? = null
	override var initialRightMargin: Int? = null

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
