package com.jonlatane.beatpad.view.palette

import android.view.ViewManager
import com.jonlatane.beatpad.view.HideableRecyclerView
import org.jetbrains.anko.custom.ankoView

fun ViewManager.editModeToolbar(
	theme: Int = 0,
	viewModel: PaletteViewModel
) = editModeToolbar(theme, viewModel, {})


inline fun ViewManager.editModeToolbar(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: EditModeToolbar.() -> Unit
) = ankoView({ EditModeToolbar(it, viewModel) }, theme, init)

inline fun ViewManager.viewModeToolbar(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: ViewModeToolbar.() -> Unit
) = ankoView({ ViewModeToolbar(it, viewModel) }, theme, init)

inline fun ViewManager.staffConfigurationToolbar(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: StaffConfigurationToolbar.() -> Unit
) = ankoView({ StaffConfigurationToolbar(it, viewModel) }, theme, init)

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