package com.jonlatane.beatpad.view.topology

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.TextView
import java.util.concurrent.atomic.AtomicInteger

/**
 * TopologyView works with three states for animation consistency.

 * Lifecycle:

 * 1. [.skipToInitialState] happens during init - all Chord views from Sequences are hidden in the center
 * 2. [.animateToSelectionPhase] animates the Chord choices out from initial state
 * 3. [.animateToTargetChord] animates the entire topology in the direction
 * of the target, such that the initial state is identical.  It will then run [.skipToInitialState]
 * and [TopologyView.updateChordText] in one step and the [.animateToSelectionPhase].

 * Created by jonlatane on 5/7/17.
 */
object NavigationAnimations {
    private val DURATION: Long = 200

    internal fun animateToTargetChord(v: TopologyView) {
        val target = v.selectedChord ?: return
        target.setTranslationZ(10f)
        val tX = target.getTranslationX()
        val tY = target.getTranslationY()
        val toTargetChord = mutableListOf<ViewPropertyAnimator>()

        var centralTY = tY
        if (v.halfStepUp === v.selectedChord || v.halfStepDown === v.selectedChord) centralTY = -tY
        toTargetChord.add(v.centralChord.animate()
                .translationXBy(-tX)
                .translationYBy(centralTY)
                .rotation(target.getRotation())
                .scaleX(target.getScaleX()).scaleY(target.getScaleY()))
        for (halfStep in arrayOf<TextView>(v.halfStepDown, v.halfStepUp)) {
            if (halfStep === v.selectedChord) {
                toTargetChord.add(
                        halfStep.animate()
                                .scaleX(CENTRAL_CHORD_SCALE).scaleY(CENTRAL_CHORD_SCALE)
                                .translationY(0f).translationZBy(10f)
                )
            } else {
                toTargetChord.add(
                        halfStep.animate()
                                .translationY(0f)
                                .alpha(0f)
                )
            }
        }
        if (v.selectedChord !== v.halfStepDown && v.selectedChord !== v.halfStepUp) {
            animateHeight(v.halfStepBackground, 5)
        }
        for (sv in v.sequences) {

            // The axis stays fixed
            if (sv.forward === target || sv.back === target) {
                animateToTargetChord(v, sv, toTargetChord, tX, tY)
            } else {
                arrayOf(sv.connectBack, sv.connectForward, sv.axis, sv.forward, sv.back).mapTo(toTargetChord) {
                    it.animate().translationXBy(-tX).translationYBy(-tY).alpha(0f)
                }
            }
        }
        afterAll(toTargetChord, Runnable {
            v.updateChordText()
            skipToInitialState(v)
            v.post { animateToSelectionPhase(v) }
        })
    }

    private fun animateToTargetChord(v: TopologyView, sv: TopologyView.SequenceViews, animators: MutableList<ViewPropertyAnimator>, tX: Float, tY: Float) {
        val targetView: View
        val targetConn: View
        val oppositeView: View
        val oppositeConn: View

        if (sv.forward === v.selectedChord) {
            targetView = sv.forward
            targetConn = sv.connectForward
            oppositeView = sv.back
            oppositeConn = sv.connectBack
        } else {
            targetView = sv.back
            targetConn = sv.connectBack
            oppositeView = sv.forward
            oppositeConn = sv.connectForward
        }
        animators.add(targetView.animate().translationX(0f).translationY(0f).scaleX(CENTRAL_CHORD_SCALE).scaleY(CENTRAL_CHORD_SCALE))
        animators.add(targetConn.animate().translationXBy(-tX).translationY(tY / 2f).alpha(1f)
                .rotation(oppositeConn.getRotation()))
        animators.add(oppositeView.animate().translationXBy(-tX).translationYBy(tY).alpha(0f))
        animators.add(oppositeConn.animate().translationXBy(-tX).translationYBy(tY).alpha(0f))
    }

    internal fun skipToInitialState(v: TopologyView) {
        v.centralChord.setScaleX(CENTRAL_CHORD_SCALE)
        v.centralChord.setScaleY(CENTRAL_CHORD_SCALE)
        v.centralChord.setTranslationX(0f)
        v.centralChord.setTranslationY(0f)
        v.centralChord.setRotation(0f)
        v.centralChord.setAlpha(1f)
        for (halfStep in arrayOf<TextView>(v.halfStepUp, v.halfStepDown)) {
            halfStep.clearAnimation()
            halfStep.setScaleX(HALF_STEP_SCALE)
            halfStep.setScaleY(HALF_STEP_SCALE)
            halfStep.setTranslationX(0f)
            halfStep.setTranslationY(0f)
            halfStep.setAlpha(1f)
            halfStep.setZ(4f)
        }
        if (v.selectedChord === v.halfStepDown) {
            v.halfStepUp.setTranslationY(-(50 + v.centralChordBackground.getHeight() / 2f))
        } else if (v.selectedChord === v.halfStepUp) {
            v.halfStepDown.setTranslationY(50 + v.centralChordBackground.getHeight() / 2f)
        }
        for (sv in v.sequences) {
            if (sv.forward === v.selectedChord || sv.back === v.selectedChord) {
                skipToSelectionPhase(v, sv)
            } else {
                for (chord in arrayOf<View>(sv.forward, sv.back, sv.axis)) {
                    chord.setScaleX(1f)
                    chord.setScaleY(1f)
                    chord.setTranslationX(0f)
                    chord.setTranslationY(0f)
                    chord.setTranslationZ(0f)
                    chord.setAlpha(0f)
                }
                val layoutParams = sv.axis.getLayoutParams()
                layoutParams.width = 0
                sv.axis.setLayoutParams(layoutParams)
                for (connector in arrayOf<View>(sv.connectBack, sv.connectForward)) {
                    connector.setTranslationX(0f)
                    connector.setTranslationY(0f)
                    connector.setTranslationZ(0f)
                    connector.setRotation(0f)
                }
            }
        }
    }

    private fun skipToSelectionPhase(v: TopologyView, sv: TopologyView.SequenceViews) {
        val theta = Math.PI / v.sequences.size
        val maxTX = v.getWidth() * 0.4f
        val maxTY = v.getHeight() * 0.4f
        var x = 0f
        var y = 0f
        var forwardAngle = 0.0
        for (i in 0..v.sequences.size - 1) {
            if (sv === v.sequences.get(i)) {
                forwardAngle = i * theta - (Math.PI - theta) / 2
                val sin = Math.sin(forwardAngle)
                val cos = Math.cos(forwardAngle)
                x = (maxTX * cos).toFloat()
                y = (maxTY * sin).toFloat()
            }
        }
        //skipAxisToSelectionPhase(sv.axis, x, y);
        skipConnectorsToSelectionPhase(sv, x, y, forwardAngle, v.selectedChord)
        skipChordsToSelectionPhase(sv, x, y, v.selectedChord)
    }

    internal fun animateToSelectionPhase(v: TopologyView) {
        val theta = Math.PI / v.sequences.size
        val maxTX = v.getWidth() * 0.4f
        val maxTY = v.getHeight() * 0.4f
        val density = v.getContext().getResources().getDisplayMetrics().density

        v.halfStepUp.animate()
                .translationY(-(50 + v.centralChordBackground.getHeight() / 2f))
                .alpha(1f).setDuration(DURATION).start()
        v.halfStepDown.animate()
                .translationY(50 + v.centralChordBackground.getHeight() / 2f)
                .alpha(1f).setDuration(DURATION).start()

        animateHeight(v.halfStepBackground, 200 + v.centralChordBackground.getHeight())
        animateWidth(v.halfStepBackground, Math.round(Math.max(
                HALF_STEP_SCALE * v.halfStepUp.getWidth(), HALF_STEP_SCALE * v.halfStepDown.getWidth()
        )))
        val centralBGWidth = Math.round(
                CENTRAL_CHORD_SCALE * (v.centralChord.getWidth() - density * CHORD_PADDING_DP))
        animateWidth(v.centralChordBackground, centralBGWidth)
        animateWidth(v.centralChordTouchPoint, centralBGWidth)
        animateWidth(v.centralChordThrobber, centralBGWidth)
        for (i in 0..v.sequences.size - 1) {
            val sv = v.sequences.get(i)
            val forwardAngle = i * theta - (Math.PI - theta) / 2
            val sin = Math.sin(forwardAngle)
            val cos = Math.cos(forwardAngle)
            val x = (maxTX * cos).toFloat()
            val y = (maxTY * sin).toFloat()
            animateChordsToSelectionPhase(v, sv, x, y)
            animateAxisToSelectionPhase(sv, x, y)
            animateConnectorsToSelectionPhase(v, sv, x, y, forwardAngle)
        }
    }

    private fun animateChordsToSelectionPhase(v: TopologyView, sv: TopologyView.SequenceViews, tX: Float, tY: Float) {
        val forwardAlpha = if (v.centralChord.getText().equals(sv.forward.getText())) 0.2f else 1f
        val backAlpha = if (v.centralChord.getText().equals(sv.back.getText())) 0.2f else 1f
        sv.forward.animate()
                .translationX(tX).translationY(tY).alpha(forwardAlpha).setDuration(DURATION).start()
        sv.back.animate()
                .translationX(-tX).translationY(tY).alpha(backAlpha).setDuration(DURATION).start()
    }

    private fun skipChordsToSelectionPhase(sv: TopologyView.SequenceViews, tX: Float, tY: Float, target: TextView?) {
        if (target === sv.forward) {
            sv.back.translationX = -tX
            sv.back.setTranslationY(tY)
            sv.back.setScaleX(1f)
            sv.back.setScaleY(1f)
            sv.back.setAlpha(1f)
            sv.forward.setTranslationX(0f)
            sv.forward.setTranslationY(0f)
            sv.forward.setScaleX(1f)
            sv.forward.setScaleY(1f)
            sv.forward.setAlpha(0f)
        } else {
            sv.forward.setTranslationX(tX)
            sv.forward.setTranslationY(tY)
            sv.forward.setScaleX(1f)
            sv.forward.setScaleY(1f)
            sv.forward.setAlpha(1f)
            sv.back.setTranslationX(0f)
            sv.back.setTranslationY(0f)
            sv.back.setScaleX(1f)
            sv.back.setScaleY(1f)
            sv.back.setAlpha(0f)
        }
        sv.forward.setTranslationZ(0f)
        sv.back.setTranslationZ(0f)
    }

    private fun animateAxisToSelectionPhase(sv: TopologyView.SequenceViews, tX: Float, tY: Float) {
        //val density = sv.axis.getContext().getResources().getDisplayMetrics().density
        val width = Math.round(2f*tX + Math.max(sv.forward.width, sv.back.width))
        val propertyAnimator = sv.axis.animate().translationY(tY).translationX(0f).alpha(0.4f)
        animateWidth(sv.axis, width)
        propertyAnimator.setDuration(DURATION).start()
    }

    private fun animateConnectorsToSelectionPhase(v: TopologyView, sv: TopologyView.SequenceViews, tX: Float, tY: Float, forwardAngle: Double) {
        val connectorWidth = (Math.sqrt((tX * tX + tY * tY).toDouble()) * .7f).toInt()
        val forwardAlpha = if (v.centralChord.text == sv.forward.text) 0.1f else 0.3f
        val backAlpha = if (v.centralChord.text == sv.back.text) 0.1f else 0.3f
        sv.connectForward.animate().translationX(tX / 2).translationY(tY / 2)
                .rotation(Math.toDegrees(forwardAngle).toFloat()).alpha(forwardAlpha).start()
        animateWidth(sv.connectForward, connectorWidth)
        sv.connectBack.animate().translationX(-tX / 2).translationY(tY / 2)
                .rotation(-Math.toDegrees(forwardAngle).toFloat()).alpha(backAlpha).start()
        animateWidth(sv.connectBack, connectorWidth)
    }

    private fun skipConnectorsToSelectionPhase(sv: TopologyView.SequenceViews, tX: Float, tY: Float, forwardAngle: Double, target: TextView?) {
        val connectorWidth = (Math.sqrt((tX * tX + tY * tY).toDouble()) * .7f).toInt()
        if (sv.forward === target) {
            sv.connectBack.translationX = -tX / 2f
            sv.connectBack.translationY = tY / 2f
            sv.connectBack.alpha = 1f
            sv.connectBack.rotation = -Math.toDegrees(forwardAngle).toFloat()
            setWidth(sv.connectBack, connectorWidth)
            sv.connectForward.translationX = 0f
            sv.connectForward.translationY = 0f
            sv.connectForward.alpha = 0f
            sv.connectForward.rotation = 0f
            setWidth(sv.connectForward, 5)
        } else {
            sv.connectForward.translationX = tX / 2
            sv.connectForward.translationY = tY / 2
            sv.connectForward.alpha = 1f
            sv.connectForward.rotation = Math.toDegrees(forwardAngle).toFloat()
            setWidth(sv.connectForward, connectorWidth)
            sv.connectBack.translationX = 0f
            sv.connectBack.translationY = 0f
            sv.connectBack.alpha = 0f
            sv.connectBack.rotation = 0f
            setWidth(sv.connectBack, 5)
        }
        sv.connectBack.z = CONNECTOR_Z
        sv.connectForward.z = CONNECTOR_Z
    }

    private fun animateWidth(v: View, width: Int) {
        val anim = ValueAnimator.ofInt(v.measuredWidth, width)
        anim.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            setWidth(v, value)
        }
        anim.setDuration(DURATION).start()
    }

    private fun animateHeight(v: View, height: Int) {
        val anim = ValueAnimator.ofInt(v.measuredHeight, height)
        anim.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            setHeight(v, value)
        }
        anim.setDuration(DURATION).start()
    }

    private fun setWidth(v: View, `val`: Int) {
        val layoutParams = v.layoutParams
        layoutParams.width = `val`
        v.layoutParams = layoutParams
    }

    private fun setHeight(v: View, `val`: Int) {
        val layoutParams = v.layoutParams
        layoutParams.height = `val`
        v.layoutParams = layoutParams
    }

    private fun afterAll(animators: Collection<ViewPropertyAnimator>, action: Runnable) {
        val completed = AtomicInteger(0)
        for (animator in animators) {
            animator.withEndAction {
                if (completed.incrementAndGet() == animators.size) {
                    action.run()
                }
            }.setDuration(DURATION).start()
        }
    }
}
