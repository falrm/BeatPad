package com.jonlatane.beatpad.view.topology;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;

import com.jonlatane.beatpad.instrument.DeviceOrientationInstrument;

/**
 * Created by jonlatane on 5/8/17.
 */

public class RhythmAnimations {
    public static void wireMelodicControl(final TopologyView v, final DeviceOrientationInstrument instrument) {
        v.centralChordBackground.setOnTouchListener(new View.OnTouchListener() {
            private ViewPropertyAnimator animator = null;
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    instrument.play();
                    v.centralChordThrobber.setAlpha(0.1f);
                    v.centralChordThrobber.setScaleX(0.5f);
                    v.centralChordThrobber.setScaleY(0.5f);
                    v.centralChordThrobber.setZ(6);
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            animator = v.centralChordThrobber.animate().scaleX(1).scaleY(1).alpha(0.3f)
                                    .setDuration(2000).setInterpolator(new DecelerateInterpolator(2));
                            animator.start();
                        }
                    });
                    return true;
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    instrument.stop();
                    v.centralChordThrobber.setZ(1);
                    if(animator != null) {
                        animator.cancel();
                    }
                }
                return false;
            }
        });
    }
}
