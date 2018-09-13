package com.jonlatane.beatpad.view.melody

import android.view.ViewManager
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.custom.ankoView


fun ViewManager.melodyElementView(theme: Int = 0)
	= melodyElementView(theme) {}

inline fun ViewManager.melodyElementView(theme: Int = 0, init: MelodyBeatView.() -> Unit)
	= ankoView({ MelodyBeatView(it) }, theme, init)

fun ViewManager.melodyToneAxis(theme: Int = 0)
	= melodyToneAxis(theme) {}

inline fun ViewManager.melodyToneAxis(theme: Int = 0, init: MelodyToneAxis.() -> Unit)
	= ankoView({ MelodyToneAxis(it) }, theme, init)


fun ViewManager.melodyEditingModifiers(theme: Int = 0)
	= melodyEditingModifiers(theme) {}

inline fun ViewManager.melodyEditingModifiers(theme: Int = 0, init: MelodyEditingModifiers.() -> Unit)
	= ankoView({ MelodyEditingModifiers(it) }, theme, init)

inline fun ViewManager.melodyToolbar(viewModel: PaletteViewModel, theme: Int = 0, init: MelodyToolbar.() -> Unit = {})
	= ankoView({ MelodyToolbar(it, viewModel) }, theme, init)