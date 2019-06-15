package com.jonlatane.beatpad.view.melody

import android.view.ViewManager
import com.jonlatane.beatpad.view.melody.toolbar.MelodyEditingModifiers
import com.jonlatane.beatpad.view.melody.toolbar.MelodyEditingToolbar
import com.jonlatane.beatpad.view.melody.toolbar.MelodyReferenceToolbar
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.melodyToneAxis(theme: Int = 0, init: MelodyToneAxis.() -> Unit = {})
	= ankoView({ MelodyToneAxis(it) }, theme, init)

inline fun ViewManager.melodyEditingModifiers(theme: Int = 0, init: MelodyEditingModifiers.() -> Unit = {})
	= ankoView({ MelodyEditingModifiers(it) }, theme, init)

inline fun ViewManager.melodyEditingToolbar(viewModel: PaletteViewModel, theme: Int = 0, init: MelodyEditingToolbar.() -> Unit = {})
	= ankoView({ MelodyEditingToolbar(it, viewModel) }, theme, init)
inline fun ViewManager.melodyReferenceToolbar(viewModel: PaletteViewModel, theme: Int = 0, init: MelodyReferenceToolbar.() -> Unit = {})
	= ankoView({ MelodyReferenceToolbar(it, viewModel) }, theme, init)