package com.jonlatane.beatpad.view;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.TextView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jonlatane.beatpad.view.TopologyView.CONNECTOR_Z;

/**
 * TopologyView works with three states for animation consistency.
 *
 * Lifecycle:
 *
 * 1. {@link #skipToInitialState} happens during init - all Chord views from Sequences are hidden in the center
 * 2. {@link #animateToSelectionPhase} animates the Chord choices out from initial state
 * 3. {@link #animateToTargetChord} animates the entire topology in the direction
 *    of the target, such that the initial state is identical.  It will then run {@link #skipToInitialState}
 *    and {@link TopologyView#updateChordText} in one step and the {@link #animateToSelectionPhase}.
 *
 * Created by jonlatane on 5/7/17.
 */
public class TopologyAnimations {
    private static final long DURATION = 250;

    static void animateCentralChordClick(final TopologyView v) {
        v.centralChord.animate().scaleX(2.5f).scaleY(2.5f).setDuration(DURATION/2).withEndAction(new Runnable() {
            @Override
            public void run() {
                v.centralChord.animate().scaleX(2f).scaleY(2f).setDuration(DURATION/2).start();
            }
        }).start();
    }

    static void animateToTargetChord(final TopologyView v) {
        View target = v.selectedChord;
        target.setTranslationZ(10);
        float tX = target.getTranslationX();
        float tY = target.getTranslationY();
        List<ViewPropertyAnimator> toTargetChord = new LinkedList<>();

        float centralTY = tY;
        if(v.halfStepUp == v.selectedChord || v.halfStepDown == v.selectedChord ) centralTY = -tY;
        toTargetChord.add(v.centralChord.animate()
                .translationXBy(-tX)
                .translationYBy(centralTY)
                .rotation(target.getRotation())
                .scaleX(target.getScaleX()).scaleY(target.getScaleY()));
        for(TextView halfStep : new TextView[] {v.halfStepDown, v.halfStepUp}) {
            if(halfStep == v.selectedChord) {
                toTargetChord.add(
                        halfStep.animate()
                                .scaleX(2).scaleY(2)
                                .rotation(0)
                                .translationY(0).translationZBy(10)
                );
            } else {
                toTargetChord.add(
                        halfStep.animate()
                                .translationY(0)
                                .alpha(0)
                );
            }
        }
        if(v.selectedChord != v.halfStepDown && v.selectedChord != v.halfStepUp) {
            animateHeight(v.halfStepBackground, 5);
        }
        for(TopologyView.SequenceViews sv : v.sequences) {

            // The axis stays fixed
            if(sv.forward == target || sv.back == target) {
                animateToTargetChord(v, sv, toTargetChord, tX, tY);
            } else {
                for(View notAlongTarget : new View[] {sv.connectBack, sv.connectForward, sv.axis, sv.forward, sv.back}) {
                    toTargetChord.add(notAlongTarget.animate().translationXBy(-tX).translationYBy(-tY).alpha(0));
                }
            }
        }
        afterAll(toTargetChord, new Runnable() {
            @Override
            public void run() {
                v.updateChordText();
                skipToInitialState(v);
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        animateToSelectionPhase(v);
                    }
                });
            }
        });
    }

    private static void animateToTargetChord(TopologyView v, TopologyView.SequenceViews sv, List<ViewPropertyAnimator> animators, float tX, float tY) {
        View targetView, targetConn, oppositeView, oppositeConn;

        if(sv.forward == v.selectedChord) {
            targetView = sv.forward;
            targetConn = sv.connectForward;
            oppositeView = sv.back;
            oppositeConn = sv.connectBack;
        } else {
            targetView = sv.back;
            targetConn = sv.connectBack;
            oppositeView = sv.forward;
            oppositeConn = sv.connectForward;
        }
        animators.add(targetView.animate().translationX(0).translationY(0).scaleX(2).scaleY(2));
        animators.add(targetConn.animate().translationXBy(-tX).translationY(tY/2).alpha(1)
                .rotation(oppositeConn.getRotation()));
        animators.add(oppositeView.animate().translationXBy(-tX).translationYBy(tY).alpha(0));
        animators.add(oppositeConn.animate().translationXBy(-tX).translationYBy(tY).alpha(0));
    }

    static void skipToInitialState(TopologyView v) {
        v.centralChord.setScaleX(2);
        v.centralChord.setScaleY(2);
        v.centralChord.setTranslationX(0);
        v.centralChord.setTranslationY(0);
        v.centralChord.setRotation(0);
        v.centralChord.setAlpha(1);
        for(TextView chord : new TextView [] {v.halfStepUp, v.halfStepDown}) {
            chord.setScaleX(0.7f);
            chord.setScaleY(0.7f);
            chord.setTranslationX(0);
            if(v.selectedChord != v.halfStepUp && v.selectedChord != v.halfStepDown) {
                chord.setTranslationY(0);
            }
            chord.setZ(4);
            chord.setRotation(-90);
            if(chord == v.selectedChord && chord == v.halfStepUp) {
                v.halfStepDown.setAlpha(1);
                v.halfStepDown.setTranslationY(v.getHeight() * 0.15f);
                v.halfStepUp.setTranslationY(0);
            } else if(chord == v.selectedChord && chord == v.halfStepDown) {
                v.halfStepUp.setAlpha(1);
                v.halfStepUp.setTranslationY(-v.getHeight() * 0.15f);
                v.halfStepDown.setTranslationY(0);
            }
        }
        for(TopologyView.SequenceViews sv : v.sequences) {
            if(sv.forward == v.selectedChord || sv.back == v.selectedChord) {
                skipToSelectionPhase(v, sv);
            } else {
                for (View chord : new View[]{sv.forward, sv.back, sv.axis}) {
                    chord.setScaleX(1);
                    chord.setScaleY(1);
                    chord.setTranslationX(0);
                    chord.setTranslationY(0);
                    chord.setTranslationZ(0);
                    chord.setAlpha(0);
                }
                ViewGroup.LayoutParams layoutParams = sv.axis.getLayoutParams();
                layoutParams.width = 0;
                sv.axis.setLayoutParams(layoutParams);
                for (View connector : new View[]{sv.connectBack, sv.connectForward}) {
                    connector.setTranslationX(0);
                    connector.setTranslationY(0);
                    connector.setTranslationZ(0);
                    connector.setRotation(0);
                }
            }
        }
    }

    private static void skipToSelectionPhase(TopologyView v, TopologyView.SequenceViews sv) {
        double theta = Math.PI / v.sequences.size();
        float maxTX = v.getWidth() * 0.4f;
        float maxTY = v.getHeight() * 0.4f;
        float x=0, y=0;
        double forwardAngle=0;
        for(int i = 0; i < v.sequences.size(); i++) {
            if(sv == v.sequences.get(i)) {
                forwardAngle = (i * theta) - ((Math.PI - theta) / 2);
                double sin = Math.sin(forwardAngle);
                double cos = Math.cos(forwardAngle);
                x = (float) (maxTX * cos);
                y = (float) (maxTY * sin);
            }
        }
        skipAxisToSelectionPhase(sv.axis, x, y);
        skipConnectorsToSelectionPhase(sv, x, y, forwardAngle, v.selectedChord);
        skipChordsToSelectionPhase(sv, x, y, v.selectedChord);
    }

    static void animateToSelectionPhase(TopologyView v) {
        double theta = Math.PI / v.sequences.size();
        float maxTX = v.getWidth() * 0.4f;
        float maxTY = v.getHeight() * 0.4f;
        v.halfStepUp.animate().translationY(-v.getHeight() * 0.15f).alpha(1).setDuration(DURATION).start();
        v.halfStepDown.animate().translationY(v.getHeight() * 0.15f).alpha(1).setDuration(DURATION).start();

        float density = v.getContext().getResources().getDisplayMetrics().density;
        animateHeight(v.halfStepBackground,
                (int) Math.max(350 * density,
                               v.getHeight() * 0.3f
                                       + Math.max(v.halfStepUp.getWidth(), v.halfStepDown.getWidth())));
        animateWidth(v.centralChordBackground,
                (int) Math.max(200 * density,
                               2 * v.centralChord.getWidth()));
        for(int i = 0; i < v.sequences.size(); i++) {
            TopologyView.SequenceViews sv = v.sequences.get(i);
            double forwardAngle = (i * theta) - ((Math.PI - theta) / 2);
            double sin = Math.sin(forwardAngle);
            double cos = Math.cos(forwardAngle);
            float x = (float)(maxTX * cos);
            float y = (float)(maxTY * sin);
            sv.forward.animate()
                    .translationX(x).translationY(y).alpha(1).setDuration(DURATION).start();
            sv.back.animate()
                    .translationX(-x).translationY(y).alpha(1).setDuration(DURATION).start();
            animateChordsToSelectionPhase(sv, x, y);
            animateAxisToSelectionPhase(sv.axis, x, y);
            animateConnectorsToSelectionPhase(sv, x, y, forwardAngle);
        }
    }
    private static void animateChordsToSelectionPhase(TopologyView.SequenceViews sv, float tX, float tY) {
        sv.forward.animate()
                .translationX(tX).translationY(tY).alpha(1).setDuration(DURATION).start();
        sv.back.animate()
                .translationX(-tX).translationY(tY).alpha(1).setDuration(DURATION).start();
    }
    private static void skipChordsToSelectionPhase(TopologyView.SequenceViews sv, float tX, float tY, TextView target) {
        if(target == sv.forward) {
            sv.back.setTranslationX(-tX);
            sv.back.setTranslationY(tY);
            sv.back.setScaleX(1);
            sv.back.setScaleY(1);
            sv.back.setAlpha(1);
            sv.forward.setTranslationX(0);
            sv.forward.setTranslationY(0);
            sv.forward.setScaleX(1);
            sv.forward.setScaleY(1);
            sv.forward.setAlpha(0);
        } else {
            sv.forward.setTranslationX(tX);
            sv.forward.setTranslationY(tY);
            sv.forward.setScaleX(1);
            sv.forward.setScaleY(1);
            sv.forward.setAlpha(1);
            sv.back.setTranslationX(0);
            sv.back.setTranslationY(0);
            sv.back.setScaleX(1);
            sv.back.setScaleY(1);
            sv.back.setAlpha(0);
        }
        sv.forward.setTranslationZ(0);
        sv.back.setTranslationZ(0);
    }

    private static void animateAxisToSelectionPhase(final View axis, float tX, float tY) {
        float density = axis.getContext().getResources().getDisplayMetrics().density;
        int width = (int) (density * (tX + 50));
        ViewPropertyAnimator propertyAnimator = axis.animate().translationY(tY).alpha(0.2f);
        animateWidth(axis, width);
        propertyAnimator.setDuration(DURATION).start();
    }

    private static void skipAxisToSelectionPhase(final View axis, float tX, float tY) {
        float density = axis.getContext().getResources().getDisplayMetrics().density;
        int width = (int) (density * (tX + 50));
        axis.setTranslationY(tY);
        axis.setTranslationZ(0);
        setWidth(axis, width);
    }

    private static void animateConnectorsToSelectionPhase(TopologyView.SequenceViews sv, float tX, float tY, double forwardAngle) {
        int connectorWidth = (int) (Math.sqrt(tX * tX + tY * tY) * .7f);
        sv.connectForward.animate().translationX(tX/2).translationY(tY/2)
                .rotation((float)Math.toDegrees(forwardAngle)).alpha(0.3f).start();
        animateWidth(sv.connectForward, connectorWidth);
        sv.connectBack.animate().translationX(-tX/2).translationY(tY/2)
                .rotation((float)-Math.toDegrees(forwardAngle)).alpha(0.3f).start();
        animateWidth(sv.connectBack, connectorWidth);
    }

    private static void skipConnectorsToSelectionPhase(TopologyView.SequenceViews sv, float tX, float tY, double forwardAngle, TextView target) {
        int connectorWidth = (int) (Math.sqrt(tX * tX + tY * tY) * .7f);
        if(sv.forward == target) {
            sv.connectBack.setTranslationX(-tX/2);
            sv.connectBack.setTranslationY(tY/2);
            sv.connectBack.setAlpha(1f);
            sv.connectBack.setRotation((float)-Math.toDegrees(forwardAngle));
            setWidth(sv.connectBack, connectorWidth);
            sv.connectForward.setTranslationX(0);
            sv.connectForward.setTranslationY(0);
            sv.connectForward.setAlpha(0f);
            sv.connectForward.setRotation(0);
            setWidth(sv.connectForward, 5);
        } else {
            sv.connectForward.setTranslationX(tX / 2);
            sv.connectForward.setTranslationY(tY / 2);
            sv.connectForward.setAlpha(1f);
            sv.connectForward.setRotation((float) Math.toDegrees(forwardAngle));
            setWidth(sv.connectForward, connectorWidth);
            sv.connectBack.setTranslationX(0);
            sv.connectBack.setTranslationY(0);
            sv.connectBack.setAlpha(0f);
            sv.connectBack.setRotation(0);
            setWidth(sv.connectBack, 5);
        }
        sv.connectBack.setZ(CONNECTOR_Z);
        sv.connectForward.setZ(CONNECTOR_Z);
    }

    private static void animateWidth(final View v, int width) {
        ValueAnimator anim = ValueAnimator.ofInt(v.getMeasuredWidth(), width);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                setWidth(v, val);
            }
        });
        anim.setDuration(DURATION);
        anim.setDuration(DURATION).start();
    }

    private static void animateHeight(final View v, int height) {
        ValueAnimator anim = ValueAnimator.ofInt(v.getMeasuredHeight(), height);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                setHeight(v, val);
            }
        });
        anim.setDuration(DURATION);
        anim.setDuration(DURATION).start();
    }

    private static void setWidth(View v, int val) {
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        layoutParams.width = val;
        v.setLayoutParams(layoutParams);
    }

    private static void setHeight(View v, int val) {
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        layoutParams.height = val;
        v.setLayoutParams(layoutParams);
    }

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
            animator.setDuration(DURATION).start();
        }
    }
}
