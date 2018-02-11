package com.jonlatane.beatpad.view.palette

import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView

fun ViewManager.paletteToolbar(
	theme: Int = 0,
	viewModel: PaletteViewModel
) = paletteToolbar(theme, viewModel, {})


inline fun ViewManager.paletteToolbar(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: PaletteToolbar.() -> Unit
) = ankoView({ PaletteToolbar(it, viewModel) }, theme, init)