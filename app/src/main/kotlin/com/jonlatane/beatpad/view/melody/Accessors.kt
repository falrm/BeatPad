package com.jonlatane.beatpad.view.melody

import android.view.ViewManager
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.melody.toolbar.*
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.melodyToneAxis(theme: Int = 0, init: MelodyToneAxis.() -> Unit = {})
	= ankoView({ MelodyToneAxis(it) }, theme, init)

inline fun ViewManager.melodyEditingModifiers(theme: Int = 0, init: MelodyEditingModifiers.() -> Unit = {})
	= ankoView({ MelodyEditingModifiers(it) }, theme, init)

inline fun ViewManager.melodyEditingToolbar(viewModel: PaletteViewModel, theme: Int = 0, init: MelodyEditingToolbar.() -> Unit = {})
	= ankoView({ MelodyEditingToolbar(it, viewModel) }, theme, init)
inline fun ViewManager.lengthToolbar(viewModel: PaletteViewModel, theme: Int = 0, init: MelodyLengthToolbar.() -> Unit = {})
	= ankoView({ MelodyLengthToolbar(it, viewModel) }, theme, init)
inline fun ViewManager.melodyReferenceToolbar(viewModel: PaletteViewModel, theme: Int = 0, init: MelodyReferenceToolbar.() -> Unit = {})
	= ankoView({ MelodyReferenceToolbar(it, viewModel) }, theme, init)
inline fun ViewManager.sectionToolbar(viewModel: PaletteViewModel, theme: Int = 0, init: SectionToolbar.() -> Unit = {})
	= ankoView({ SectionToolbar(it, viewModel) }, theme, init)
inline fun ViewManager.melodyView(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: HideableRelativeLayout.() -> Unit
) = ankoView({ MelodyView(it, viewModel) }, theme, init)