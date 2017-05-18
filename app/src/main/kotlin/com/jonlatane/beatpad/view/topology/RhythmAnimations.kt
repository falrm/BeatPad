package com.jonlatane.beatpad.view.topology

import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.DecelerateInterpolator

import com.jonlatane.beatpad.instrument.DeviceOrientationInstrument

/**
 * Created by jonlatane on 5/8/17.
 */

object RhythmAnimations {
    fun wireMelodicControl(v: TopologyView, instrument: DeviceOrientationInstrument) {
        v.centralChordTouchPoint.setOnTouchListener(object : View.OnTouchListener {
            private var animator: ViewPropertyAnimator? = null
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    instrument.play()
                    v.centralChordThrobber.setAlpha(0.1f)
                    v.centralChordThrobber.setScaleX(0.5f)
                    v.centralChordThrobber.setScaleY(0.5f)
                    v.centralChordThrobber.setZ(6f)
                    v.post {
                        animator = v.centralChordThrobber.animate().scaleX(1f).scaleY(1f).alpha(0.3f)
                                .setDuration(2000).setInterpolator(DecelerateInterpolator(2f))
                        animator!!.start()
                    }
                    return true
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    instrument.stop()
                    v.centralChordThrobber.setZ(1f)
                    if (animator != null) {
                        animator!!.cancel()
                    }
                }
                return false
            }
        })
    }
}
