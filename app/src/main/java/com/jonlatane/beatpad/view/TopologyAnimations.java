package com.jonlatane.beatpad.view;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewPropertyAnimator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jonlatane on 5/7/17.
 */
public class TopologyAnimations {
    static void animateToTargetChord(final TopologyView v, View target) {
        float tX = target.getTranslationX();
        float tY = target.getTranslationY();
        List<ViewPropertyAnimator> toTargetChord = new LinkedList<>();
        for(View chord : allChords(v)) {
            ViewPropertyAnimator a = chord.animate().translationXBy(-tX).translationYBy(-tY);
            if(chord != target) {
                a = a.alpha(0);
            } else {
                a = a.scaleX(2).scaleY(2);
            }
            toTargetChord.add(a);
        }
        afterAll(toTargetChord, new Runnable() {
            @Override
            public void run() {
                v.updateChordText();
                skipToInitialState(v);
                animateToSelectionPhase(v);
            }
        });
    }

    static void animateToSelectionPhase(TopologyView v) {
        double theta = Math.PI / v.sequences.size();
        float maxTX = v.getWidth() * 0.4f;
        float maxTY = v.getHeight() * 0.4f;
        v.halfStepUp.animate().translationY(-v.getHeight() * 0.15f).alpha(1).start();
        v.halfStepDown.animate().translationY(v.getHeight() * 0.15f).alpha(1).start();
        for(int i = 0; i < v.sequences.size(); i++) {
            TopologyView.SequenceViews sv = v.sequences.get(i);
            double forwardAngle = (i * theta) - ((Math.PI - theta) / 2);
            double sin = Math.sin(forwardAngle);
            double cos = Math.cos(forwardAngle);
            float x = (float)(maxTX * cos);
            float y = (float)(maxTY * sin);
            sv.forward.animate()
                    .translationX(x).translationY(-y).alpha(1).start();
            sv.back.animate()
                    .translationX(-x).translationY(y).alpha(1).start();

            //TODO figure out something better to do with connectors
            sv.connectForward.animate().scaleY(2).translationX(x/2).translationY(-y/2)
                    .rotation((float)-Math.toDegrees(forwardAngle)).alpha(0).start();
            sv.connectBack.animate().scaleY(2).translationX(-x/2).translationY(y/2)
                    .rotation((float)-Math.toDegrees(forwardAngle)).alpha(0).start();
        }
    }

    static void skipToInitialState(TopologyView v) {
        v.currentChord.setScaleX(2);
        v.currentChord.setScaleY(2);
        v.currentChord.setTranslationX(0);
        v.currentChord.setTranslationY(0);
        v.currentChord.setAlpha(1);
        for(View chord : new View [] {v.halfStepUp, v.halfStepDown}) {
            chord.setScaleX(0.7f);
            chord.setScaleY(0.7f);
            chord.setTranslationX(0);
            chord.setTranslationY(0);
            chord.setAlpha(0);
        }
        for(TopologyView.SequenceViews sv : v.sequences) {
            for(View chord : new View[] {sv.forward, sv.back, sv.connectForward, sv.connectBack}) {
                chord.setScaleX(1);
                chord.setScaleY(1);
                chord.setTranslationX(0);
                chord.setTranslationY(0);
                chord.setAlpha(0);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private static void afterAll(final Collection<ViewPropertyAnimator> animators, final Runnable action) {
        final AtomicInteger completed = new AtomicInteger(0);
        for(ViewPropertyAnimator animator : animators) {
            animator = animator.withEndAction(new Runnable() {
                @Override
                public void run() {
                    if(completed.incrementAndGet() == animators.size()) {
                        action.run();
                    }
                }
            });
            animator.start();
        }
    }

    private static List<View> allChords(TopologyView v) {
        List<View> result = new LinkedList<>();
        result.add(v.currentChord);
        result.add(v.halfStepDown);
        result.add(v.halfStepUp);

        for(int i = 0; i < v.sequences.size(); i++) {
            TopologyView.SequenceViews sv = v.sequences.get(i);
            result.add(sv.forward);
            result.add(sv.back);
            result.add(sv.connectForward);
            result.add(sv.connectBack);
        }
        return result;
    }
}
