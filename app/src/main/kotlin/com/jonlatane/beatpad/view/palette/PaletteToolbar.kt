package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity.*
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.LinearLayout
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.hide
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.util.show
import com.jonlatane.beatpad.view.nonDelayedHorizontalScrollView
import com.jonlatane.beatpad.view.nonDelayedScrollView
import com.jonlatane.beatpad.view.tonesequence.*
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onScrollChange

inline fun ViewManager.paletteToolbar(
	theme: Int = 0,
	viewModel: PaletteViewModel
) = paletteToolbar(theme, viewModel, {})

inline fun ViewManager.paletteToolbar(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: _LinearLayout.() -> Unit
) = ankoView({
	_LinearLayout(it).apply {
		orientation = LinearLayout.HORIZONTAL
		button {
			text = "Play"
		}.lparams {
			width = matchParent
			height = wrapContent
			weight = 1f
		}
		button {
			text = "Stop"
		}.lparams {
			width = matchParent
			height = wrapContent
			weight = 1f
		}
		button {
			text = "Boards"
		}.lparams {
			width = matchParent
			height = wrapContent
			weight = 1f
		}.onClick {
			if(viewModel.keyboardView.isHidden)
				viewModel.keyboardView.show()
			else
				viewModel.keyboardView.hide()
		}
		backgroundColor = context.color(R.color.colorPrimaryDark)
	}
}, theme, init)