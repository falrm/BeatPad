package com.jonlatane.beatpad.view

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector


class ZoomableRecyclerView @JvmOverloads constructor(
	context: Context,
	scrollingEnabled: Boolean = true,
	/**
	 * Parameters: xDelta, yDelta
	 */
	var zoomHandler: ((Float, Float) -> Boolean) = { _, _ -> false },
	var zoomFinishedHandler: () -> Unit = {}
) : NonDelayedRecyclerView(context, scrollingEnabled) {

	private var isScaling = false
	private val scaleDetector = ScaleGestureDetector(
		context,
		object : ScaleGestureDetector.OnScaleGestureListener {
			override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
				isScaling = true
				return true
			}
			override fun onScaleEnd(detector: ScaleGestureDetector?) {
				isScaling = false
				zoomFinishedHandler()
			}
			override fun onScale(detector: ScaleGestureDetector): Boolean {
				val (xDelta, yDelta) = detector.run {
					currentSpanX - previousSpanX to currentSpanY - previousSpanY
				}
				return zoomHandler(xDelta, yDelta)
			}
		}
	).apply {
		isQuickScaleEnabled = false
	}

	override fun dispatchTouchEvent(event: MotionEvent): Boolean {
		if(scrollingEnabled) {
			scaleDetector.onTouchEvent(event)
		}
		return super.dispatchTouchEvent(event)
	}
}
