package com.jonlatane.beatpad.view.orbifold

import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.DecelerateInterpolator

import com.jonlatane.beatpad.output.controller.DeviceOrientationInstrument
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.concurrent.atomic.AtomicInteger

object RhythmAnimations: AnkoLogger {
	fun wireMelodicControl(v: OrbifoldView, instrument: DeviceOrientationInstrument) {
		info("Setting ontouch")
		v.centralChordTouchPoint.setOnTouchListener(object : View.OnTouchListener {
			private var animator: ViewPropertyAnimator? = null
			val pointerCount = AtomicInteger(0)
			override fun onTouch(view: View, event: MotionEvent): Boolean {
				info("Rhythm onTouch")
				return when (event.actionMasked) {
					ACTION_DOWN, ACTION_POINTER_DOWN -> {
						pointerCount.incrementAndGet()
						instrument.play()
						v.centralChordThrobber.alpha = 0.1f
						v.centralChordThrobber.scaleX = 0.5f
						v.centralChordThrobber.scaleY = 0.5f
						v.centralChordThrobber.z = Float.MAX_VALUE
						v.post {
							animator = v.centralChordThrobber.animate().scaleX(1f).scaleY(1f).alpha(0.3f)
								.setDuration(2000).setInterpolator(DecelerateInterpolator(2f))
							animator!!.start()
						}
						true
					}
					ACTION_UP, ACTION_POINTER_UP -> {
						if (pointerCount.decrementAndGet() == 0) {
							instrument.stop()
							v.centralChordThrobber.z = Float.MIN_VALUE
							if (animator != null) {
								animator!!.cancel()
							}
							v.centralChordThrobber.alpha = 0f
						}
						false
					}
					else -> false
				}
			}
		})
	}
}
