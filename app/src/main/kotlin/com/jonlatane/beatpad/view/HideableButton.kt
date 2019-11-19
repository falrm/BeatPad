package com.jonlatane.beatpad.view

import android.widget.Button
import com.jonlatane.beatpad.util.HideableView
import org.jetbrains.anko._RelativeLayout

open class HideableButton(ctx: android.content.Context): Button(ctx), HideableView {
	override var initialHeight: Int? = null
	override var initialWidth: Int? = null
	override var initialTopMargin: Int? = null
	override var initialBottomMargin: Int? = null
	override var initialLeftMargin: Int? = null
	override var initialRightMargin: Int? = null
}