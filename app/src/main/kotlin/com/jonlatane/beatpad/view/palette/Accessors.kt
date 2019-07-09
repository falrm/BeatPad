package com.jonlatane.beatpad.view.palette

import android.view.ViewManager
import com.jonlatane.beatpad.view.HideableRecyclerView
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

inline fun ViewManager.partListView(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: HideableRecyclerView.() -> Unit
): PartListView = ankoView({ PartListView(it, viewModel) }, theme, init)


inline fun ViewManager.beatScratchToolbar(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: BeatScratchToolbar.() -> Unit = {}
) = ankoView({ BeatScratchToolbar(it, viewModel) }, theme, init)