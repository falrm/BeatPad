package com.jonlatane.beatpad.view.melody

import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView


fun ViewManager.toneSequenceElement(theme: Int = 0)
	= toneSequenceElement(theme) {}

inline fun ViewManager.toneSequenceElement(theme: Int = 0, init: MelodyElementView.() -> Unit)
	= ankoView({ MelodyElementView(it) }, theme, init)

fun ViewManager.toneSequenceAxis(theme: Int = 0)
	= toneSequenceAxis(theme) {}

inline fun ViewManager.toneSequenceAxis(theme: Int = 0, init: MelodyToneAxis.() -> Unit)
	= ankoView({ MelodyToneAxis(it) }, theme, init)


fun ViewManager.bottomScroller(theme: Int = 0)
	= bottomScroller(theme) {}

inline fun ViewManager.bottomScroller(theme: Int = 0, init: BottomScroller.() -> Unit)
	= ankoView({ BottomScroller(it) }, theme, init)