package com.jonlatane.beatpad.view.orbifold

import android.view.View
import android.widget.TextView
import com.jonlatane.beatpad.util.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

object SelectionState : NavigationState, AnkoLogger {
  override fun animateTo(v: OrbifoldView) {
    val theta = Math.PI / v.sequences.size
    val maxTX = v.width * 0.4f
    val maxTY = v.height * 0.4f

    v.halfStepUp.animate()
      .translationY(-(v.halfStepUp.scaledHeight + v.centralChord.scaledHeight) / 2f)
      .translationZ(0f)
      .alpha(1f).setDuration(ANIMATION_DURATION).start()
    v.halfStepDown.animate()
      .translationY((v.halfStepDown.scaledHeight + v.centralChord.scaledHeight) / 2f)
      .translationZ(0f)
      .alpha(1f).setDuration(ANIMATION_DURATION).start()

    v.halfStepBackground.animateHeight(
      height = v.halfStepUp.scaledHeight + v.halfStepDown.scaledHeight
        + v.centralChord.scaledHeight + Math.round(15 * v.density),
      duration = ANIMATION_DURATION
    )
    v.halfStepBackground.animateWidth(
      width = Math.max(v.halfStepUp.scaledWidth, v.halfStepDown.scaledWidth) + Math.round(15 * v.density),
      duration = ANIMATION_DURATION
    )
    v.centralChordTouchPoint.animateWidth(
      width = v.centralChord.scaledWidth,
      duration = ANIMATION_DURATION
    )
    v.centralChordThrobber.animateWidth(
      width = v.centralChord.scaledWidth,
      duration = ANIMATION_DURATION
    )
    for (i in 0..v.sequences.size - 1) {
      val sv = v.sequences[i]
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

  override fun skipTo(v: OrbifoldView) = TODO("Not needed")

  internal fun skipToSelectionPhase(v: OrbifoldView, sv: OrbifoldView.SequenceViews) {
    val theta = Math.PI / v.sequences.size
    val maxTX = v.width * 0.4f
    val maxTY = v.height * 0.4f
    var x = 0f
    var y = 0f
    var forwardAngle = 0.0
    for (i in 0..v.sequences.size - 1) {
      if (sv === v.sequences[i]) {
        forwardAngle = i * theta - (Math.PI - theta) / 2
        val sin = Math.sin(forwardAngle)
        val cos = Math.cos(forwardAngle)
        x = (maxTX * cos).toFloat()
        y = (maxTY * sin).toFloat()
      }
    }
    skipAxisToSelectionPhase(sv, x, y);
    skipConnectorsToSelectionPhase(sv, x, y, forwardAngle, v.selectedChord)
    skipChordsToSelectionPhase(sv, x, y, v.selectedChord)
  }

  private fun skipAxisToSelectionPhase(sv: OrbifoldView.SequenceViews, tX: Float, tY: Float) {
    val width = Math.round(2f * tX + Math.max(sv.forward.width, sv.back.width))
    info("Setting to axis width $width")
    sv.axis.apply {
      alpha = 0.4f
      translationX = 0f
      translationY = tY
      layoutWidth = width
    }
  }

  private fun skipChordsToSelectionPhase(sv: OrbifoldView.SequenceViews, tX: Float, tY: Float, target: TextView?) {
    if (target === sv.forward) {
      sv.back.apply {
        translationX = -tX
        translationY = tY
        scaleX = 1f
        scaleY = 1f
        alpha = 1f
      }
      sv.forward.apply {
        translationX = 0f
        translationY = 0f
        scaleX = 1f
        scaleY = 1f
        alpha = 0f
      }
    } else {
      sv.forward.apply {
        translationX = tX
        translationY = tY
        scaleX = 1f
        scaleY = 1f
        alpha = 1f
      }
      sv.back.apply {
        translationX = 0f
        translationY = 0f
        scaleX = 1f
        scaleY = 1f
        alpha = 0f
      }
    }
    sv.forward.translationZ = 0f
    sv.back.translationZ = 0f
  }

  private fun skipConnectorsToSelectionPhase(sv: OrbifoldView.SequenceViews, tX: Float, tY: Float, forwardAngle: Double, target: TextView?) {
    val connectorWidth = (Math.sqrt((tX * tX + tY * tY).toDouble()) * .7f).toInt()
    if (sv.forward === target) {
      sv.connectBack.translationX = -tX / 2f
      sv.connectBack.translationY = tY / 2f
      sv.connectBack.alpha = 1f
      sv.connectBack.rotation = -Math.toDegrees(forwardAngle).toFloat()
      sv.connectBack.layoutWidth = connectorWidth
      sv.connectForward.translationX = 0f
      sv.connectForward.translationY = 0f
      sv.connectForward.alpha = 0f
      sv.connectForward.rotation = 0f
      sv.connectForward.layoutWidth = 5
    } else {
      sv.connectForward.translationX = tX / 2
      sv.connectForward.translationY = tY / 2
      sv.connectForward.alpha = 1f
      sv.connectForward.rotation = Math.toDegrees(forwardAngle).toFloat()
      sv.connectForward.layoutWidth = connectorWidth
      sv.connectBack.translationX = 0f
      sv.connectBack.translationY = 0f
      sv.connectBack.alpha = 0f
      sv.connectBack.rotation = 0f
      sv.connectBack.layoutWidth = 5
    }
    sv.connectBack.elevation = sv.connectBack.connectorElevation
    sv.connectForward.elevation = sv.connectForward.connectorElevation
  }

  private fun animateChordsToSelectionPhase(v: OrbifoldView, sv: OrbifoldView.SequenceViews, tX: Float, tY: Float) {
    val forwardAlpha = if (v.centralChord.text == sv.forward.text) 0.2f else 1f
    val backAlpha = if (v.centralChord.text == sv.back.text) 0.2f else 1f
    sv.forward.animate()
      .translationX(tX).translationY(tY).alpha(forwardAlpha).setDuration(ANIMATION_DURATION).start()
    sv.back.animate()
      .translationX(-tX).translationY(tY).alpha(backAlpha).setDuration(ANIMATION_DURATION).start()
  }

  private fun animateAxisToSelectionPhase(sv: OrbifoldView.SequenceViews, tX: Float, tY: Float) {
    val width = Math.round(2f * tX + Math.max(sv.forward.width, sv.back.width))
    info("Animating to axis width $width")
    val propertyAnimator = sv.axis.animate().translationY(tY).translationX(0f).alpha(0.4f)
    sv.axis.animateWidth(width)
    propertyAnimator.setDuration(ANIMATION_DURATION).start()
  }

  private fun animateConnectorsToSelectionPhase(v: OrbifoldView, sv: OrbifoldView.SequenceViews, tX: Float, tY: Float, forwardAngle: Double) {
    val connectorWidth = (Math.sqrt((tX * tX + tY * tY).toDouble()) * .7f).toInt()
    val forwardAlpha = if (v.centralChord.text == sv.forward.text) 0.1f else 0.3f
    val backAlpha = if (v.centralChord.text == sv.back.text) 0.1f else 0.3f
    sv.connectForward.animate().translationX(tX / 2).translationY(tY / 2)
      .rotation(Math.toDegrees(forwardAngle).toFloat()).alpha(forwardAlpha).start()
    sv.connectForward.animateWidth(connectorWidth, ANIMATION_DURATION)
    sv.connectBack.animate().translationX(-tX / 2).translationY(tY / 2)
      .rotation(-Math.toDegrees(forwardAngle).toFloat()).alpha(backAlpha).start()
    sv.connectBack.animateWidth(connectorWidth, ANIMATION_DURATION)
  }


}