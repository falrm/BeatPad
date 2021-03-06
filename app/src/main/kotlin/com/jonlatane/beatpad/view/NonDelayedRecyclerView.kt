package com.jonlatane.beatpad.view

import android.view.MotionEvent
import com.jonlatane.beatpad.util.HideableView
import org.jetbrains.anko._RelativeLayout
import org.jetbrains.anko.recyclerview.v7._RecyclerView

open class NonDelayedRecyclerView @JvmOverloads constructor(
	ctx: android.content.Context,
	var scrollingEnabled: Boolean = true
): HideableRecyclerView(ctx) {
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