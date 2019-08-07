package com.jonlatane.beatpad.view

import com.jonlatane.beatpad.util.HideableView
import org.jetbrains.anko._RelativeLayout
import org.jetbrains.anko.constraint.layout._ConstraintLayout

open class HideableConstraintLayout(ctx: android.content.Context): _ConstraintLayout(ctx), HideableView {
	override var initialHeight: Int? = null
	override var initialWidth: Int? = null
}