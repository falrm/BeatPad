package com.jonlatane.beatpad.view.colorboard

import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView


fun ViewManager.colorboardView(theme: Int = 0)
	= colorboardView(theme) {}

inline fun ViewManager.colorboardView(theme: Int = 0, init: ColorboardInputView.() -> Unit)
	= ankoView({ ColorboardInputView(it) }, theme, init)
