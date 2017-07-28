package com.jonlatane.beatpad.view.topology

import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.topologyView(theme: Int = 0)
	= topologyView(theme) {}

inline fun ViewManager.topologyView(theme: Int = 0, init: TopologyView.() -> Unit)
	= ankoView({ TopologyView(it) }, theme, init)