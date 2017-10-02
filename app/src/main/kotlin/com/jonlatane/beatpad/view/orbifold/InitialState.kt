package com.jonlatane.beatpad.view.orbifold

import android.view.View
import android.view.ViewPropertyAnimator
import com.jonlatane.beatpad.util.*
import com.jonlatane.beatpad.view.orbifold.SelectionState.skipToSelectionPhase

/**
 * Created by jonlatane on 5/23/17.
 */
object InitialState : NavigationState {
  /**
   * Animates to the initial state with the target
   */
  override fun animateTo(v: OrbifoldView) {
    val target = v.selectedChord ?: return
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
            .translationXYBy(-tX, -tY)
            .alpha(0f)
        )
      }
    }
    if (v.selectedChord !== v.halfStepDown && v.selectedChord !== v.halfStepUp) {
      v.halfStepBackground.animateHeight(5, ANIMATION_DURATION)
    }
    for (sv in v.sequences) {

      // The axis stays fixed
      if (sv.forward === target || sv.back === target) {
        animateToTargetChord(v, sv, toTargetChord, tX, tY)
      } else {
        toTargetChord.addAll(arrayOf(sv.connectBack, sv.connectForward, sv.axis, sv.forward, sv.back).mapTo(toTargetChord) {
          it.animate().translationXBy(-tX).translationYBy(-tY).alpha(0f)
        })
      }
    }
    afterAll(toTargetChord, ANIMATION_DURATION) {
      v.updateChordText()
      v.skipTo(InitialState)
      v.post { v.animateTo(SelectionState) }
    }
  }

  override fun skipTo(v: OrbifoldView) {
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
        elevation = v.halfStepChordElevation
      }
    }
    if (v.selectedChord === v.halfStepDown) {
      v.halfStepUp.translationY = -(v.halfStepUp.scaledHeight + v.centralChord.scaledHeight) / 2f
      v.halfStepUp.translationZ = 0f
    } else if (v.selectedChord === v.halfStepUp) {
      v.halfStepDown.translationY = (v.halfStepDown.scaledHeight + v.centralChord.scaledHeight) / 2f
      v.halfStepDown.translationZ = 0f
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
        sv.axis.layoutWidth = 1
        for (connector in arrayOf(sv.connectBack, sv.connectForward)) {
          connector.apply {
            translationXY = 0f
            translationZ = 0f
            rotation = 0f
          }
        }
      }
    }
  }

  private fun animateToTargetChord(v: OrbifoldView, sv: OrbifoldView.SequenceViews, animators: MutableList<ViewPropertyAnimator>, tX: Float, tY: Float) {
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
    animators.add(targetView.animate().translationXY(0f).translationZ(v.centralChord.z - targetView.z).scale(CENTRAL_CHORD_SCALE))
    animators.add(targetConn.animate().translationXBy(-tX).translationY(tY / 2f).alpha(1f)
      .rotation(oppositeConn.rotation))
    animators.add(oppositeView.animate().translationXYBy(-tX, tY).alpha(0f))
    animators.add(oppositeConn.animate().translationXYBy(-tX, tY).alpha(0f))
  }
}