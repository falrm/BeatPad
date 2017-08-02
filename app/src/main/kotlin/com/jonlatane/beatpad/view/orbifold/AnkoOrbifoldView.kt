package com.jonlatane.beatpad.view.orbifold

import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView

fun ViewManager.orbifoldView(theme: Int = 0)
	= orbifoldView(theme) {}

inline fun ViewManager.orbifoldView(theme: Int = 0, init: OrbifoldView.() -> Unit)
	= ankoView({ OrbifoldView(it) }, theme, init)