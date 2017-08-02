package com.jonlatane.beatpad.view

import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView


fun ViewManager.nonDelayedHorizontalScrollView(theme: Int = 0)
	= nonDelayedHorizontalScrollView(theme) {}

inline fun ViewManager.nonDelayedHorizontalScrollView(theme: Int = 0, init: NonDelayedHorizontalScrollView.() -> Unit)
	= ankoView({ NonDelayedHorizontalScrollView(it) }, theme, init)

fun ViewManager.nonDelayedScrollView(theme: Int = 0)
	= nonDelayedScrollView(theme) {}

inline fun ViewManager.nonDelayedScrollView(theme: Int = 0, init: NonDelayedScrollView.() -> Unit)
	= ankoView({ NonDelayedScrollView(it) }, theme, init)