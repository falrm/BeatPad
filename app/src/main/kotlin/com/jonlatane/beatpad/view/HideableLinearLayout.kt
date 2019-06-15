package com.jonlatane.beatpad.view

import com.jonlatane.beatpad.util.HideableView
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko._RelativeLayout

open class HideableLinearLayout(ctx: android.content.Context): _LinearLayout(ctx), HideableView {
	override var initialHeight: Int? = null
	override var initialWidth: Int? = null
}