package com.jonlatane.beatpad.view

import com.jonlatane.beatpad.util.HideableView
import org.jetbrains.anko._RelativeLayout

class HideableRelativeLayout(ctx: android.content.Context): _RelativeLayout(ctx), HideableView {
	override var initialHeight: Int? = null
}