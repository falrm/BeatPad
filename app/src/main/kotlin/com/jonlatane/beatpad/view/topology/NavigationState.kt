package com.jonlatane.beatpad.view.topology

interface NavigationState {
    fun animateTo(v: TopologyView)
    fun skipTo(v: TopologyView)
}
