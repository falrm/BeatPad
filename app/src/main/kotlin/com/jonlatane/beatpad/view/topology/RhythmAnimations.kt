package com.jonlatane.beatpad.view.topology

import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.DecelerateInterpolator

import com.jonlatane.beatpad.output.controller.DeviceOrientationInstrument

/**
 * Created by jonlatane on 5/8/17.
 */
object RhythmAnimations {
    fun wireMelodicControl(v: TopologyView, instrument: DeviceOrientationInstrument) {
        v.centralChordTouchPoint.setOnTouchListener(object : View.OnTouchListener {
            private var animator: ViewPropertyAnimator? = null
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
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
                    return true
                } else if (event.action == MotionEvent.ACTION_UP) {
                    instrument.stop()
                    v.centralChordThrobber.z = Float.MIN_VALUE
                    if (animator != null) {
                        animator!!.cancel()
                    }
                    v.centralChordThrobber.alpha = 0f
                }
                return false
            }
        })
    }
}
