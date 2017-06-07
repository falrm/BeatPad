package com.jonlatane.beatpad.util

import android.view.View
import com.jonlatane.beatpad.view.topology.animateHeight
import com.jonlatane.beatpad.view.topology.layoutHeight

interface HideableView {
	var initialHeight: Int?
}
val View.isHidden: Boolean get() = layoutHeight == 0
fun View.show(animated: Boolean = true) {
	if(animated) {
		animateHeight((this as HideableView).initialHeight!!)
	} else {
		layoutHeight = (this as HideableView).initialHeight!!
	}
}
fun View.hide(animated: Boolean = true) {
	if ((this as HideableView).initialHeight == null) {
		initialHeight = if(height > 0) height else layoutHeight
	}
	if(animated) {
		animateHeight(0)
	} else {
		layoutHeight = 0
	}
}