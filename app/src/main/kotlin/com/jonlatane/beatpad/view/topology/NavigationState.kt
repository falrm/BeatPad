package com.jonlatane.beatpad.view.topology

/**
 * Created by jonlatane on 5/23/17.
 */
interface NavigationState {
    fun animateTo(v: TopologyView)
    fun skipTo(v: TopologyView)
}

fun TopologyView.skipTo(state: NavigationState) {
    state.skipTo(this)
}

fun TopologyView.animateTo(state: NavigationState) {
    state.animateTo(this)
}