package com.jonlatane.beatpad.view.topology

import android.widget.TextView

/**
 * Created by jonlatane on 5/23/17.
 */
object SelectionState : NavigationState {
    override fun animateTo(v: TopologyView) {

        val theta = Math.PI / v.sequences.size
        val maxTX = v.width * 0.4f
        val maxTY = v.height * 0.4f
        val density = v.context.resources.displayMetrics.density

        v.halfStepUp.animate()
                .translationY(-(50 + v.centralChordBackground.height / 2f))
                .alpha(1f).setDuration(ANIMATION_DURATION).start()
        v.halfStepDown.animate()
                .translationY(50 + v.centralChordBackground.height / 2f)
                .alpha(1f).setDuration(ANIMATION_DURATION).start()

        v.halfStepBackground.animateHeight(200 + v.centralChordBackground.height)
        v.halfStepBackground.animateWidth(Math.round(Math.max(
                HALF_STEP_SCALE * v.halfStepUp.width, HALF_STEP_SCALE * v.halfStepDown.width
        )))
        val centralBGWidth = Math.round(
                CENTRAL_CHORD_SCALE * (v.centralChord.width - density * CHORD_PADDING_DP))
        v.centralChordBackground.animateWidth(centralBGWidth)
        v.centralChordTouchPoint.animateWidth(centralBGWidth)
        v.centralChordThrobber.animateWidth(centralBGWidth)
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

    override fun skipTo(v: TopologyView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



}
internal fun skipToSelectionPhase(v: TopologyView, sv: TopologyView.SequenceViews) {
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
    //skipAxisToSelectionPhase(sv.axis, x, y);
    skipConnectorsToSelectionPhase(sv, x, y, forwardAngle, v.selectedChord)
    skipChordsToSelectionPhase(sv, x, y, v.selectedChord)
}

private fun skipChordsToSelectionPhase(sv: TopologyView.SequenceViews, tX: Float, tY: Float, target: TextView?) {
    if (target === sv.forward) {
        sv.back.translationX = -tX
        sv.back.translationY = tY
        sv.back.scaleX = 1f
        sv.back.scaleY = 1f
        sv.back.alpha = 1f
        sv.forward.translationX = 0f
        sv.forward.translationY = 0f
        sv.forward.scaleX = 1f
        sv.forward.scaleY = 1f
        sv.forward.alpha = 0f
    } else {
        sv.forward.translationX = tX
        sv.forward.translationY = tY
        sv.forward.scaleX = 1f
        sv.forward.scaleY = 1f
        sv.forward.alpha = 1f
        sv.back.translationX = 0f
        sv.back.translationY = 0f
        sv.back.scaleX = 1f
        sv.back.scaleY = 1f
        sv.back.alpha = 0f
    }
    sv.forward.translationZ = 0f
    sv.back.translationZ = 0f
}

private fun skipConnectorsToSelectionPhase(sv: TopologyView.SequenceViews, tX: Float, tY: Float, forwardAngle: Double, target: TextView?) {
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
    sv.connectBack.z = CONNECTOR_Z
    sv.connectForward.z = CONNECTOR_Z
}

private fun animateChordsToSelectionPhase(v: TopologyView, sv: TopologyView.SequenceViews, tX: Float, tY: Float) {
    val forwardAlpha = if (v.centralChord.text == sv.forward.text) 0.2f else 1f
    val backAlpha = if (v.centralChord.text == sv.back.text) 0.2f else 1f
    sv.forward.animate()
            .translationX(tX).translationY(tY).alpha(forwardAlpha).setDuration(ANIMATION_DURATION).start()
    sv.back.animate()
            .translationX(-tX).translationY(tY).alpha(backAlpha).setDuration(ANIMATION_DURATION).start()
}

private fun animateAxisToSelectionPhase(sv: TopologyView.SequenceViews, tX: Float, tY: Float) {
    //val density = sv.axis.getContext().getResources().getDisplayMetrics().density
    val width = Math.round(2f*tX + Math.max(sv.forward.width, sv.back.width))
    val propertyAnimator = sv.axis.animate().translationY(tY).translationX(0f).alpha(0.4f)
    sv.axis.animateWidth(width)
    propertyAnimator.setDuration(ANIMATION_DURATION).start()
}

private fun animateConnectorsToSelectionPhase(v: TopologyView, sv: TopologyView.SequenceViews, tX: Float, tY: Float, forwardAngle: Double) {
    val connectorWidth = (Math.sqrt((tX * tX + tY * tY).toDouble()) * .7f).toInt()
    val forwardAlpha = if (v.centralChord.text == sv.forward.text) 0.1f else 0.3f
    val backAlpha = if (v.centralChord.text == sv.back.text) 0.1f else 0.3f
    sv.connectForward.animate().translationX(tX / 2).translationY(tY / 2)
            .rotation(Math.toDegrees(forwardAngle).toFloat()).alpha(forwardAlpha).start()
    sv.connectForward.animateWidth(connectorWidth)
    sv.connectBack.animate().translationX(-tX / 2).translationY(tY / 2)
            .rotation(-Math.toDegrees(forwardAngle).toFloat()).alpha(backAlpha).start()
    sv.connectBack.animateWidth(connectorWidth)
}