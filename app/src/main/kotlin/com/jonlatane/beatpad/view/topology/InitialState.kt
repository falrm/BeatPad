package com.jonlatane.beatpad.view.topology

import android.view.View
import android.view.ViewPropertyAnimator

/**
 * Created by jonlatane on 5/23/17.
 */
object InitialState : NavigationState {
    /**
     * Animates to the initial state with the target
     */
    override fun animateTo(v: TopologyView) {
        val target = v.selectedChord ?: return
        target.translationZ = 10f
        val tX = target.translationX
        val tY = target.translationY
        val toTargetChord = mutableListOf<ViewPropertyAnimator>()

        var centralTY = tY
        if (v.halfStepUp === v.selectedChord || v.halfStepDown === v.selectedChord) centralTY = -tY
        toTargetChord.add(v.centralChord.animate()
                .translationXBy(-tX)
                .translationYBy(centralTY)
                .rotation(target.rotation)
                .scaleX(target.scaleX).scaleY(target.scaleY))
        for (halfStep in arrayOf(v.halfStepDown, v.halfStepUp)) {
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
            v.halfStepBackground.animateHeight(5)
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
        afterAll(toTargetChord) {
            v.updateChordText()
            v.skipTo(InitialState)
            v.post { v.animateTo(SelectionState) }
        }
    }

    override fun skipTo(v: TopologyView) {
        v.centralChord.apply {
            scale = CENTRAL_CHORD_SCALE
            translationXY = 0f
            rotation = 0f
            alpha = 1f
        }
        for (halfStep in arrayOf(v.halfStepUp, v.halfStepDown)) {
            halfStep.apply {
                clearAnimation()
                scale = HALF_STEP_SCALE
                translationXY = 0f
                alpha = 1f
                z = 4f
            }
        }
        if (v.selectedChord === v.halfStepDown) {
            v.halfStepUp.translationY = -(50 + v.centralChordBackground.height / 2f)
        } else if (v.selectedChord === v.halfStepUp) {
            v.halfStepDown.translationY = 50 + v.centralChordBackground.height / 2f
        }
        for (sv in v.sequences) {
            if (sv.forward === v.selectedChord || sv.back === v.selectedChord) {
                skipToSelectionPhase(v, sv)
            } else {
                for (chord in arrayOf(sv.forward, sv.back, sv.axis)) {
                    chord.apply {
                        scale = 1f
                        translationXY = 0f
                        translationZ = 0f
                        alpha = 0f
                    }
                }
                val layoutParams = sv.axis.layoutParams
                layoutParams.width = 0
                sv.axis.layoutParams = layoutParams
                for (connector in arrayOf<View>(sv.connectBack, sv.connectForward)) {
                    connector.apply {
                        translationXY = 0f
                        translationZ = 0f
                        rotation = 0f
                    }
                }
            }
        }
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
        animators.add(targetView.animate().translationXY(0f).scale(CENTRAL_CHORD_SCALE))
        animators.add(targetConn.animate().translationXBy(-tX).translationY(tY / 2f).alpha(1f)
                .rotation(oppositeConn.getRotation()))
        animators.add(oppositeView.animate().translationXYBy(-tX,tY).alpha(0f))
        animators.add(oppositeConn.animate().translationXYBy(-tX,tY).alpha(0f))
    }
}