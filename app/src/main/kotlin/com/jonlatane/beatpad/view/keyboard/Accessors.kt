package com.jonlatane.beatpad.view.keyboard

import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView


fun ViewManager.keyboardView(theme: Int = 0)
	= keyboardView(theme) {}

inline fun ViewManager.keyboardView(theme: Int = 0, init: KeyboardView.() -> Unit)
	= ankoView({ KeyboardView(it) }, theme, init)
