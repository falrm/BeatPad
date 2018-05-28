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

fun ViewManager.zoomableScrollView(theme: Int = 0)
	= zoomableScrollView(theme) {}

inline fun ViewManager.zoomableScrollView(theme: Int = 0, init: ZoomableScrollView.() -> Unit)
	= ankoView({ ZoomableScrollView(it) }, theme, init)

fun ViewManager.noDefaultSpinner(theme: Int = 0)
	= noDefaultSpinner(theme) {}

inline fun ViewManager.noDefaultSpinner(theme: Int = 0, init: NoDefaultSpinner.() -> Unit)
	= ankoView({ NoDefaultSpinner(it) }, theme, init)


fun ViewManager.nonDelayedRecyclerView(theme: Int = 0)
	= nonDelayedRecyclerView(theme) {}

inline fun ViewManager.nonDelayedRecyclerView(theme: Int = 0, init: NonDelayedRecyclerView.() -> Unit)
	= ankoView({ NonDelayedRecyclerView(it) }, theme, init)