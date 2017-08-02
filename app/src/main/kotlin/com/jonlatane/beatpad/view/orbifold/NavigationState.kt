package com.jonlatane.beatpad.view.orbifold

interface NavigationState {
    fun animateTo(v: OrbifoldView)
    fun skipTo(v: OrbifoldView)
}
