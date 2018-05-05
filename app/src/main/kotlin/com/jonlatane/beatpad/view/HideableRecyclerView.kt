package com.jonlatane.beatpad.view

import com.jonlatane.beatpad.util.HideableView
import org.jetbrains.anko._RelativeLayout
import org.jetbrains.anko.recyclerview.v7._RecyclerView

open class HideableRecyclerView(ctx: android.content.Context): _RecyclerView(ctx), HideableView {
	override var initialHeight: Int? = null
}