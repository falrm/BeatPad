package com.jonlatane.beatpad.view

import android.graphics.Typeface
import android.view.ViewManager
import com.jonlatane.beatpad.MainApplication
import org.jetbrains.anko.custom.ankoView


//fun ViewManager.numberPickerWithTypeface(theme: Int = 0)
//	= numberPickerWithTypeface(theme) {}
//inline fun ViewManager.numberPickerWithTypeface(theme: Int = 0, init: NumberPickerWithTypeface.() -> Unit)
//	= ankoView({ NumberPickerWithTypeface(it) }, theme, init)


inline fun ViewManager.hideableLinearLayout(theme: Int = 0, init: HideableLinearLayout.() -> Unit = {})
	= ankoView({ HideableLinearLayout(it) }, theme, init)

fun ViewManager.nonDelayedHorizontalScrollView(theme: Int = 0)
	= nonDelayedHorizontalScrollView(theme) {}

inline fun ViewManager.nonDelayedHorizontalScrollView(theme: Int = 0, init: NonDelayedHorizontalScrollView.() -> Unit)
	= ankoView({ NonDelayedHorizontalScrollView(it) }, theme, init)

fun ViewManager.nonDelayedScrollView(theme: Int = 0)
	= nonDelayedScrollView(theme) {}

inline fun ViewManager.nonDelayedScrollView(theme: Int = 0, init: NonDelayedScrollView.() -> Unit)
	= ankoView({ NonDelayedScrollView(it) }, theme, init)

fun ViewManager.noDefaultSpinner(theme: Int = 0)
	= noDefaultSpinner(theme) {}

inline fun ViewManager.noDefaultSpinner(theme: Int = 0, init: NoDefaultSpinner.() -> Unit)
	= ankoView({ NoDefaultSpinner(it) }, theme, init)

inline fun ViewManager.numberPickerWithTypeface(
	theme: Int = 0,
	init: NumberPickerWithTypeface.() -> Unit = {}
)
	= ankoView({ NumberPickerWithTypeface(it) }, theme, init)


fun ViewManager.hideableRecyclerView(theme: Int = 0)
  = hideableRecyclerView(theme) {}

inline fun ViewManager.hideableRecyclerView(theme: Int = 0, init: HideableRecyclerView.() -> Unit)
	= ankoView({ HideableRecyclerView(it) }, theme, init)

fun ViewManager.nonDelayedRecyclerView(theme: Int = 0)
	= nonDelayedRecyclerView(theme) {}

inline fun ViewManager.nonDelayedRecyclerView(theme: Int = 0, init: NonDelayedRecyclerView.() -> Unit)
	= ankoView({ NonDelayedRecyclerView(it) }, theme, init)
inline fun ViewManager.hideableRelativeLayout(theme: Int = 0, init: HideableRelativeLayout.() -> Unit)
	= ankoView({ HideableRelativeLayout(it) }, theme, init)
inline fun ViewManager.hideableConstraintLayout(theme: Int = 0, init: HideableConstraintLayout.() -> Unit)
	= ankoView({ HideableConstraintLayout(it) }, theme, init)

fun ViewManager.zoomableRecyclerView(theme: Int = 0)
	= zoomableRecyclerView(theme) {}

inline fun ViewManager.zoomableRecyclerView(theme: Int = 0, init: ZoomableRecyclerView.() -> Unit)
	= ankoView({ ZoomableRecyclerView(it) }, theme, init)