package com.jonlatane.beatpad.view.library

import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView


fun ViewManager.libraryPaletteView(theme: Int = 0)
	= libraryPaletteView(theme) {}

inline fun ViewManager.libraryPaletteView(
	theme: Int = 0,
	init: LibraryPaletteView.() -> Unit
)
	= ankoView({ LibraryPaletteView(it) }, theme, init)
