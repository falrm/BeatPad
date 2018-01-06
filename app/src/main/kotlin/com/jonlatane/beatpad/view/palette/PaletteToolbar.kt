package com.jonlatane.beatpad.view.palette

import android.view.ViewManager
import android.widget.LinearLayout
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.hide
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.util.show
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.sdk25.coroutines.onClick

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
		backgroundColor = context.color(R.color.colorPrimaryDark)

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
			text = "Keys"
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

		button {
			text = "Colors"
		}.lparams {
			width = matchParent
			height = wrapContent
			weight = 1f
		}.onClick {
			if(viewModel.colorboardView.isHidden)
				viewModel.colorboardView.show()
			else
				viewModel.colorboardView.hide()
		}
	}
}, theme, init)